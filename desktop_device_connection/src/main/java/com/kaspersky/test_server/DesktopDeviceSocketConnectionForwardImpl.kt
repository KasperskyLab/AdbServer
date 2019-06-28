package com.kaspersky.test_server

import com.kaspersky.test_server.api.AdbCommand
import com.kaspersky.test_server.api.AdbCommandExecutor
import com.kaspresky.test_server.log.Logger
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

internal class DesktopDeviceSocketConnectionForwardImpl(
    private val logger: Logger
) : DesktopDeviceSocketConnection {

    companion object {
        private const val DEVICE_PORT: Int = 8500
        private const val LOCAL_HOST: String = "127.0.0.1"
        private const val DESKTOP_MIN_PORT = 9000
    }

    private val serverSocket by lazy { ServerSocket(DEVICE_PORT) }
    private val lastClientPort = AtomicInteger(DESKTOP_MIN_PORT)

    override fun getDesktopSocketLoad(executor: AdbCommandExecutor): () -> Socket {
        val clientPort = getFreePort()
        logger.i(javaClass.simpleName, "clientPort=$clientPort")
        forwardPorts(executor, clientPort, DEVICE_PORT)
        return {
            logger.i(javaClass.simpleName, "getDesktopSocketLoad() with ip=$LOCAL_HOST, port=$clientPort start")
            val readyClientSocket = Socket(LOCAL_HOST, clientPort)
            logger.i(javaClass.simpleName, "getDesktopSocketLoad() with ip=$LOCAL_HOST, port=$clientPort success")
            readyClientSocket
        }
    }

    private fun getFreePort(): Int {
        return lastClientPort.incrementAndGet()
    }

    private fun forwardPorts(executor: AdbCommandExecutor, fromPort: Int, toPort: Int) {
        logger.i(javaClass.simpleName, "forwardPorts(fromPort=$fromPort, toPort=$toPort) start")
        val result = executor.execute(AdbCommand("forward tcp:$fromPort tcp:$toPort"))
        logger.i(javaClass.simpleName, "forwardPorts(fromPort=$fromPort, toPort=$toPort) result=$result")
    }

    override fun getDeviceSocketLoad(): () -> Socket = {
        logger.i(javaClass.simpleName, "getDeviceSocketLoad() start")
        val readyServerSocket = serverSocket.accept()
        logger.i(javaClass.simpleName, "getDeviceSocketLoad() success")
        readyServerSocket
    }

}