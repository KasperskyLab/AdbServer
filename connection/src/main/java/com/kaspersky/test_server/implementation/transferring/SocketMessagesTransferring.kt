package com.kaspersky.test_server.implementation.transferring

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

// todo logging, comments
internal class SocketMessagesTransferring<ReceiveModel, SendModel> private constructor(
    private val socket: Socket,
    private val receiveModelClass: Class<ReceiveModel>,
    private val sendModelClass: Class<SendModel>
) {

    companion object {
        inline fun <reified Receive, reified Send> createTransferring(socket: Socket): SocketMessagesTransferring<Receive, Send> {
            return SocketMessagesTransferring(socket, Receive::class.java, Send::class.java)
        }
    }

    private lateinit var inputStream: ObjectInputStream
    private lateinit var outputStream: ObjectOutputStream
    private lateinit var messagesListeningThread: Thread
    private lateinit var messagesListener: MessagesListener<ReceiveModel>

    fun startListening(listener: MessagesListener<ReceiveModel>) {
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
        messagesListeningThread = MessagesListeningThread()
        messagesListeningThread.start()
    }

    fun sendMessage(sendModel: SendModel) {
        try {
            outputStream.writeObject(sendModel)
            outputStream.flush()
        } catch (e: IOException) {
            // todo think about exception handling
            if (!socket.isClosed) {
                e.printStackTrace()
            }
        }
    }

    fun stopListening() {
        messagesListeningThread.interrupt()
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
                messagesListener.listenMessages(obj as ReceiveModel)
            } else {
                // todo exception name
                throw IllegalStateException("Unknown received object: $obj")
            }
        } catch (e: Exception) {
            // todo logs or disconnect?
        }
    }

}