package com.kaspersky.test_server.implementation.transferring

import com.kaspersky.test_server.implementation.light_socket.LightSocketWrapper
import com.kaspresky.test_server.log.Logger
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.atomic.AtomicBoolean

// todo logging, comments
internal class SocketMessagesTransferring<ReceiveModel, SendModel> private constructor(
    private val lightSocketWrapper: LightSocketWrapper,
    private val receiveModelClass: Class<ReceiveModel>,
    private val sendModelClass: Class<SendModel>,
    private val logger: Logger,
    private val disruptAction: () -> Unit
) {

    companion object {
        inline fun <reified Receive, reified Send> createTransferring(
            lightSocketWrapper: LightSocketWrapper,
            logger: Logger,
            noinline disruptAction: () -> Unit
        ): SocketMessagesTransferring<Receive, Send> {
            return SocketMessagesTransferring(
                lightSocketWrapper,
                Receive::class.java,
                Send::class.java,
                logger,
                disruptAction
            )
        }
    }

    private lateinit var inputStream: ObjectInputStream
    private lateinit var outputStream: ObjectOutputStream
    private lateinit var messagesListener: (ReceiveModel) -> Unit
    private val isRunning: AtomicBoolean = AtomicBoolean(false)

    fun startListening(listener: (ReceiveModel) -> Unit) {
        logger.i(javaClass.simpleName, "startListening")
        messagesListener = listener
        try {
            outputStream = ObjectOutputStream(lightSocketWrapper.getOutputStream())
            inputStream = ObjectInputStream(lightSocketWrapper.getInputStream())
        } catch (exception: Exception) {
            disruptAction.invoke()
            return
        }
        startHandleMessages()
    }

    private fun startHandleMessages() {
        isRunning.set(true)
        MessagesListeningThread().start()
    }

    fun sendMessage(sendModel: SendModel) {
        logger.i(javaClass.simpleName, "sendMessage(sendModel=$sendModel)")
        try {
            outputStream.writeObject(sendModel)
            outputStream.flush()
        } catch (exception: Exception) {
            // todo think about exception handling
            logger.e(javaClass.simpleName, "sendMessage(sendModel=$sendModel) failed! " +
                    "isRunning = ${isRunning.get()}. exception=$exception"
            )
        }
    }

    fun stopListening() {
        isRunning.set(false)
    }

    private inner class MessagesListeningThread : Thread("MessagesListeningThread for lightSocketWrapper = $lightSocketWrapper") {
        override fun run() {
            while (isRunning.get()) {
                peekNextMessage()
            }
        }
    }

    private fun peekNextMessage() {
        val obj: Any
        try {
            obj = inputStream.readObject()
            // todo check this
            if (obj.javaClass == receiveModelClass) {
                logger.i(javaClass.simpleName, "peekNextMessage(message=$obj)")
                messagesListener.invoke(obj as ReceiveModel)
            } else {
                // todo exception name
                logger.e(javaClass.simpleName, "peekNextMessage(message=$obj) but this message type is not $receiveModelClass")
                disruptAction.invoke()
            }
        } catch (e: Exception) {
            logger.e(javaClass.simpleName, "peekNextMessage failed with ${e.message.toString()}")
            disruptAction.invoke()
        }
    }

}