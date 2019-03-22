package com.kaspersky.test_server_command_handler.remote

import com.kaspersky.test_server_command_handler.Command
import com.kaspersky.test_server_command_handler.ICommandExecutor
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

private fun Exception.stackTraceToString(): String {
    val stringWriter = StringWriter()
    printStackTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}

interface Logger {
    fun print(msg: String)
}

class DefaultLogger(private val tag: String? = null) : Logger {
    override fun print(msg: String) {
        if (tag == null) {
            System.out.println(msg)
        } else {
            System.out.println("$tag: $msg")
        }
    }
}

object EmptyLogger : Logger {
    override fun print(msg: String) = Unit
}

class RemoteCommandExecutor private constructor(
        private val ip: String?,
        private val port: Int,
        private val commandExecutor: ICommandExecutor,
        private val isServer: Boolean,
        private val logger: Logger
) : IRemoteCommandExecutor {
    companion object {
        @Suppress("UNUSED")
        fun server(port: Int,
                   remoteCommandExecutor: ICommandExecutor,
                   logger: Logger = DefaultLogger()
        ) = RemoteCommandExecutor(null, port, remoteCommandExecutor, true, logger)

        @Suppress("UNUSED")
        fun client(ip: String,
                   port: Int,
                   remoteCommandExecutor: ICommandExecutor,
                   logger: Logger = DefaultLogger()
        ) = RemoteCommandExecutor(ip, port, remoteCommandExecutor, false, logger)
    }

    private val serverSocket by lazy {
        ServerSocket(port)
    }
    private val bgExecutor = Executors.newCachedThreadPool()
    private var currentConnection: SocketConnection? = null

    override val isConnected: Boolean
        get() = currentConnection?.connectionState == ConnectionState.CONNECTED


    @Throws(IOException::class)
    override
    fun disconnect() {
        logger.print("disconnect started")
        try {
            currentConnection?.disconnect()
            logger.print("disconnect finished")
        } catch (e: IOException) {
            logger.print(e.stackTraceToString())
            throw e
        }
    }

    override fun <T> execute(command: Command<T>): T {
        return currentConnection?.execute(command) ?: throw IOException("not connected")
    }

    @Throws(IOException::class)
    @Synchronized
    override fun connect() {
        logger.print("connect started")
        try {
            if (isServer) {
                connectAsServer()
            } else {
                connectAsClient()
            }
            logger.print("connect finished")
        } catch (e: IOException) {
            logger.print(e.stackTraceToString())
            throw e
        }
    }

    @Throws(IOException::class)
    private fun connectAsServer() {
        if (isConnected) {
            return
        } else {
            val socket = serverSocket.accept()
            currentConnection = SocketConnection(socket,
                    commandExecutor,
                    bgExecutor,
                    logger)
            currentConnection?.connect()
        }
    }

    @Throws(IOException::class)
    private fun connectAsClient() {
        if (isConnected) {
            return
        } else {
            val socket = Socket(ip, port)
            currentConnection = SocketConnection(socket,
                    commandExecutor,
                    bgExecutor,
                    logger)
            currentConnection?.connect()
        }
    }
}