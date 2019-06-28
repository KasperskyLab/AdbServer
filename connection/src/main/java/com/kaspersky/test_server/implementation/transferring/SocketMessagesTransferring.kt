package com.kaspersky.test_server.implementation.transferring

import com.kaspresky.test_server.log.Logger
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

// todo logging, comments
internal class SocketMessagesTransferring<ReceiveModel, SendModel> private constructor(
    private val socket: Socket,
    private val receiveModelClass: Class<ReceiveModel>,
    private val sendModelClass: Class<SendModel>,
    private val logger: Logger
) {

    companion object {
        inline fun <reified Receive, reified Send> createTransferring(socket: Socket, logger: Logger): SocketMessagesTransferring<Receive, Send> {
            return SocketMessagesTransferring(socket, Receive::class.java, Send::class.java, logger)
        }
    }

    private lateinit var inputStream: ObjectInputStream
    private lateinit var outputStream: ObjectOutputStream
    private lateinit var messagesListener: MessagesListener<ReceiveModel>

    fun startListening(listener: MessagesListener<ReceiveModel>) {
        logger.i(javaClass.simpleName, "startListening")
        messagesListener = listener
        createIOStreams(socket)
        startHandleMessages()
    }

    @Throws(IOException::class)
    private fun createIOStreams(socket: Socket) {
        outputStream = ObjectOutputStream(socket.getOutputStream())
        inputStream = ObjectInputStream(socket.getInputStream())
    }

    private fun startHandleMessages() {
        MessagesListeningThread().start()
    }

    fun sendMessage(sendModel: SendModel) {
        logger.i(javaClass.simpleName, "sendMessage(sendModel=$sendModel)")
        try {
            outputStream.writeObject(sendModel)
            outputStream.flush()
        } catch (e: IOException) {
            // todo think about exception handling
            if (!socket.isClosed) {
                logger.e(javaClass.simpleName, "sendMessage(sendModel=$sendModel) failed with ${e.message.toString()}")
            }
        }
    }

    private inner class MessagesListeningThread : Thread("MessagesListeningThread for socket = $socket") {
        override fun run() {
            while (!socket.isClosed) {
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
                messagesListener.listenMessages(obj as ReceiveModel)
            } else {
                // todo exception name
                logger.e(javaClass.simpleName, "peekNextMessage(message=$obj) but this message type is not $receiveModelClass")
            }
        } catch (e: Exception) {
            logger.e(javaClass.simpleName, "peekNextMessage failed with ${e.message.toString()}")
        }
    }

}