package com.kaspersky.test_server_command_handler.remote

import com.kaspersky.test_server_command_handler.Command
import com.kaspersky.test_server_command_handler.ICommandExecutor
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.Socket
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong

internal class SocketConnection(
        private val socket: Socket,
        private val localCommandExecutor: ICommandExecutor,
        private val bgTaskExecutor: Executor,
        private val logger: Logger) {

    companion object {
        private val COMMAND_TIMEOUT_MIN = TimeUnit.MINUTES.toSeconds(3)
    }

    @Volatile
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    private lateinit var inputStream: ObjectInputStream
    private lateinit var outputStream: ObjectOutputStream
    private lateinit var commandListenThread: Thread

    private val receiversMap = ConcurrentHashMap<Long, CommandResultReceiver>()
    private val commandId = AtomicLong()

    @Synchronized
    @Throws(IOException::class)
    fun disconnect() {
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            throw IllegalStateException("Unexpected connection state = [$connectionState] appeared during disconnect")
        }
        if (connectionState == ConnectionState.DISCONNECTED) {
            return
        } else {
            try {
                connectionState = ConnectionState.DISCONNECTING
                receiversMap.forEach {
                    val id = it.key
                    deliverDisconnectResult(id)
                }
                receiversMap.clear()
                socket.close()
            } finally {
                connectionState = ConnectionState.DISCONNECTED
            }
        }
    }

    private fun deliverDisconnectResult(id: Long) {
        deliverResult(ResultMessage.Error(id, IOException("Connection has been lost")))
    }

    @Synchronized
    @Throws(IOException::class)
    fun connect() {
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            throw IllegalStateException("Unexpected connection state = [$connectionState] appeared during connect")
        }
        if (connectionState == ConnectionState.CONNECTED) {
            return
        } else {
            connectionState = ConnectionState.CONNECTING
            connectionState = try {
                createIOStreams(socket)
                startCommandListenThread()
                ConnectionState.CONNECTED
            } catch (e: Exception) {
                ConnectionState.DISCONNECTED
                throw e
            }
        }
    }

    @Throws(Exception::class)
    fun <T> execute(command: Command<T>): T {
        val id = commandId.incrementAndGet()
        val receiver = CommandResultReceiver()
        synchronized(this) {
            receiversMap[id] = receiver
            if (connectionState != ConnectionState.CONNECTED) {
                deliverDisconnectResult(id)
            } else {
                bgTaskExecutor.execute {
                    sendCommand(command, id)
                }
            }
        }

        val msg = receiver.waitResult(COMMAND_TIMEOUT_MIN, TimeUnit.SECONDS)
        receiversMap.remove(id)
        if (msg == null) {
            throw TimeoutException("Command execution timeout")
        } else {
            when (msg) {
                is ResultMessage.Success -> return msg.data as T
                is ResultMessage.Error -> throw msg.exception
            }
        }
    }

    private fun startCommandListenThread() {
        commandListenThread = CommandListenThread()
        commandListenThread.start()
    }

    private fun peekNextMessage() {
        val obj: Any
        try {
            obj = inputStream.readObject()
            when (obj) {
                is ExecuteMsg<*> -> handleCommand(obj.command, obj.id)
                is ResultMessage -> deliverResult(obj)
                else -> throw IllegalStateException("Unknown received object: $obj")
            }
        } catch (e: Exception) {
            disconnect()
        }
    }

    @Throws(IOException::class)
    private fun createIOStreams(socket: Socket) {
        outputStream = ObjectOutputStream(socket.getOutputStream())
        inputStream = ObjectInputStream(socket.getInputStream())
    }

    private fun handleCommand(command: Command<*>, id: Long) {
        bgTaskExecutor.execute {
            if (socket.isClosed) {
                return@execute
            }
            try {
                val result = localCommandExecutor.execute(command)
                sendResultMessage(ResultMessage.Success(id, result!!))
            } catch (e: Exception) {
                if (!socket.isClosed) {
                    sendResultMessage(ResultMessage.Error(id, e))
                }
            }
        }
    }

    private fun deliverResult(resultMessage: ResultMessage) {
        receiversMap[resultMessage.id]?.setResult(resultMessage)
    }

    private fun sendResultMessage(resultMessage: ResultMessage) {
        sendObject(resultMessage)
    }

    private fun sendCommand(command: Command<*>, id: Long) {
        val msg = ExecuteMsg(command, id)
        sendObject(msg)
    }

    private fun sendObject(`object`: Any) {
        try {
            outputStream.writeObject(`object`)
            outputStream.flush()
        } catch (e: IOException) {
            if (!socket.isClosed) {
                e.printStackTrace()
            }
        }

    }

    private inner class CommandListenThread : Thread("RemoveCommands listen thread") {
        override fun run() {
            while (!socket.isClosed) {
                peekNextMessage()
            }
        }
    }
}

internal enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED
}

private class CommandResultReceiver {
    @Volatile
    private var resultMessage: ResultMessage? = null
    private val waitLatch = CountDownLatch(1)

    fun setResult(resultMessage: ResultMessage) {
        this.resultMessage = resultMessage
        waitLatch.countDown()
    }

    fun waitResult(timeout: Long, unit: TimeUnit): ResultMessage? {
        try {
            waitLatch.await(timeout, unit)
        } catch (ignored: InterruptedException) {
        }

        return resultMessage
    }
}

private sealed class ResultMessage : Serializable {
    abstract val id: Long

    data class Success(override val id: Long, val data: Any) : ResultMessage()
    data class Error(override val id: Long, val exception: Exception) : ResultMessage()
}

private data class ExecuteMsg<T>(val command: Command<T>, val id: Long) : Serializable