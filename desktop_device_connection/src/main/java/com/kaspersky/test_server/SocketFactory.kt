package com.kaspersky.test_server

import com.kaspresky.test_server.log.Logger
import com.kaspresky.test_server.log.LoggerFactory
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

// todo maybe add wrapper for Socket java class
object SocketFactory {

    private const val DEVICE_PORT: Int = 8500
    private const val DESKTOP_IP: String = "127.0.0.1"
    private const val DESKTOP_MIN_PORT = 9000

    private val serverSocket by lazy { ServerSocket(DEVICE_PORT) }
    private val lastClientPort = AtomicInteger(DESKTOP_MIN_PORT)
    private val logger: Logger = LoggerFactory.systemLogger()

    fun getDeviceSocket(): () -> Socket = {
        logger.i(javaClass.simpleName, "getDeviceSocket() start")
        val readyServerSocket = serverSocket.accept()
        logger.i(javaClass.simpleName, "getDeviceSocket() success")
        readyServerSocket
    }

    fun getDesktopSocket(): () -> Socket = {
        val port = getFreePort()
        logger.i(javaClass.simpleName, "getDesktopSocket() with ip=$DESKTOP_IP, port=$port start")
        val readyClientSocket = Socket(DESKTOP_IP, getFreePort())
        logger.i(javaClass.simpleName, "getDesktopSocket() with ip=$DESKTOP_IP, port=$port success")
        readyClientSocket
    }

    private fun getFreePort(): Int {
        return lastClientPort.incrementAndGet()
    }

}