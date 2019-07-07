package com.kaspersky.test_server.implementation.transferring

import com.kaspersky.test_server.implementation.light_socket.LightSocketWrapper
import com.kaspresky.test_server.log.Logger
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.atomic.AtomicBoolean

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

    private val tag = javaClass.simpleName
    private lateinit var inputStream: ObjectInputStream
    private lateinit var outputStream: ObjectOutputStream
    private lateinit var messagesListener: (ReceiveModel) -> Unit
    private val isRunning: AtomicBoolean = AtomicBoolean(false)

    fun startListening(listener: (ReceiveModel) -> Unit) {
        logger.i(tag, "startListening", "start")
        messagesListener = listener
        try {
            outputStream = ObjectOutputStream(lightSocketWrapper.getOutputStream())
            inputStream = ObjectInputStream(lightSocketWrapper.getInputStream())
            logger.i(tag, "startListening", "IO Streams were created")
        } catch (exception: Exception) {
            logger.e(tag, "startListening", exception)
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
        logger.i(tag, "sendMessage","where sendModel=$sendModel")
        try {
            outputStream.writeObject(sendModel)
            outputStream.flush()
        } catch (exception: Exception) {
            logger.e(tag, "sendMessage", exception)
        }
    }

    fun stopListening() {
        isRunning.set(false)
    }

    private inner class MessagesListeningThread : Thread() {
        override fun run() {
            logger.i("$tag.MessagesListeningThread", "run", "start to work")
            while (isRunning.get()) {
                peekNextMessage()
            }
        }
    }

    private fun peekNextMessage() {
        val obj: Any
        try {
            obj = inputStream.readObject()
            if (obj.javaClass == receiveModelClass) {
                logger.i("$tag.MessagesListeningThread", "peekNextMessage", "with message=$obj")
                messagesListener.invoke(obj as ReceiveModel)
            } else {
                logger.i("$tag.MessagesListeningThread", "peekNextMessage", "with message=$obj" +
                        " but this message type is not $receiveModelClass"
                )
                disruptAction.invoke()
            }
        } catch (exception: Exception) {
            logger.e("$tag.MessagesListeningThread", "peekNextMessage", exception)
            disruptAction.invoke()
        }
    }

}