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
        private val LAST_CLIENT_PORT = AtomicInteger(DESKTOP_MIN_PORT)
    }

    private val tag = javaClass.simpleName
    private val serverSocket by lazy { ServerSocket(DEVICE_PORT) }

    override fun getDesktopSocketLoad(executor: AdbCommandExecutor): () -> Socket {
        val clientPort = getFreePort()
        logger.i(tag, "getDesktopSocketLoad", "calculated desktop client port=$clientPort")
        forwardPorts(executor, clientPort, DEVICE_PORT)
        logger.i(tag, "getDesktopSocketLoad", "desktop client port=$clientPort is forwarding with device server port=$DEVICE_PORT")
        return {
            logger.i(tag, "getDesktopSocketLoad", "started with ip=$LOCAL_HOST, port=$clientPort")
            val readyClientSocket = Socket(LOCAL_HOST, clientPort)
            logger.i(tag, "getDesktopSocketLoad", "completed with ip=$LOCAL_HOST, port=$clientPort")
            readyClientSocket
        }
    }

    private fun getFreePort(): Int {
        return LAST_CLIENT_PORT.incrementAndGet()
    }

    private fun forwardPorts(executor: AdbCommandExecutor, fromPort: Int, toPort: Int) {
        logger.i(tag, "forwardPorts(fromPort=$fromPort, toPort=$toPort)", "started")
        val result = executor.execute(AdbCommand("forward tcp:$fromPort tcp:$toPort"))
        logger.i(tag, "forwardPorts(fromPort=$fromPort, toPort=$toPort)", "result=$result")
    }

    override fun getDeviceSocketLoad(): () -> Socket = {
        logger.i(tag, "getDeviceSocketLoad", "started")
        val readyServerSocket = serverSocket.accept()
        logger.i(tag, "getDeviceSocketLoad", "completed")
        readyServerSocket
    }

}