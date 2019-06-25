package com.kaspersky.test_server.implementation

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

// todo maybe add wrapper for Socket java class
internal object SocketFactory {

    private const val SERVER_PORT: Int = 8500
    private const val IP: String = "127.0.0.1"
    private const val MIN_CLIENT_PORT_VALUE = 9000

    private val serverSocket by lazy { ServerSocket(SERVER_PORT) }
    private val lastClientPort = AtomicInteger(MIN_CLIENT_PORT_VALUE)

    fun getServerSocket(): Socket = serverSocket.accept()

    fun getClientSocket(): Socket = Socket(IP, getFreePort())

    private fun getFreePort(): Int {
        return lastClientPort.incrementAndGet()
    }

}