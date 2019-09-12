package com.kaspersky.test_server

import com.kaspersky.test_server.api.CommandExecutor
import com.kaspresky.test_server.log.Logger
import java.net.ServerSocket
import java.net.Socket
import kotlin.random.Random

internal class DesktopDeviceSocketConnectionForwardImpl(
    private val logger: Logger
) : DesktopDeviceSocketConnection {

    companion object {
        private const val DEVICE_PORT: Int = 8500
        private const val MIN_CLIENT_PORT: Int = 6000
        private const val MAX_CLIENT_PORT: Int = 49000
        private const val LOCAL_HOST: String = "127.0.0.1"
    }

    private val tag = javaClass.simpleName
    private val clientPortsList: MutableList<Int> = mutableListOf()

    override fun getDesktopSocketLoad(executor: CommandExecutor): () -> Socket {
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
        var newClientPort: Int
        while (true) {
            newClientPort = Random.Default.nextInt(MIN_CLIENT_PORT, MAX_CLIENT_PORT)
            if (!clientPortsList.contains(newClientPort)) {
                break
            }
            clientPortsList.add(newClientPort)
        }
        return newClientPort
    }

    private fun forwardPorts(executor: CommandExecutor, fromPort: Int, toPort: Int) {
        logger.i(tag, "forwardPorts(fromPort=$fromPort, toPort=$toPort)", "started")
        val result = executor.execute(AdbCommand("forward tcp:$fromPort tcp:$toPort"))
        logger.i(tag, "forwardPorts(fromPort=$fromPort, toPort=$toPort)", "result=$result")
    }

    override fun getDeviceSocketLoad(): () -> Socket = {
        logger.i(tag, "getDeviceSocketLoad", "started")
        val serverSocket = ServerSocket(DEVICE_PORT)
        val readyServerSocket = serverSocket.accept()
        logger.i(tag, "getDeviceSocketLoad", "completed")
        readyServerSocket
    }
}