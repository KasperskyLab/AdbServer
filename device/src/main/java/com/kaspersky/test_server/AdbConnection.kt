package com.kaspersky.test_server

import com.kaspersky.test_server.api.AdbCommand
import com.kaspersky.test_server.api.ConnectionClient
import com.kaspersky.test_server.api.ConnectionFactory
import com.kaspresky.test_server.log.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object AdbConnection {

    private const val CONNECTION_ESTABLISH_TIMEOUT_SEC = 5L
    private val logger = LoggerFactory.systemLogger()
    private lateinit var connectionClient: ConnectionClient
    private val isRunning = AtomicReference<Boolean>()

    @Synchronized
    fun start() {
        logger.i(javaClass.simpleName, "start() start")
        val desktopDeviceSocketConnection =
            DesktopDeviceSocketConnectionFactory.getSockets(
                DesktopDeviceSocketConnectionType.FORWARD,
                logger
            )
        connectionClient = ConnectionFactory.getClient(
            desktopDeviceSocketConnection.getDeviceSocketLoad(),
            logger
        )
        isRunning.set(true)
        WatchdogThread().start()
    }

    @Synchronized
    fun stop() {
        logger.i(javaClass.simpleName, "stop() start")
        isRunning.set(false)
        connectionClient.disconnect()
    }

    // todo think about return value
    fun execute(command: String): String {
        awaitConnectionEstablished(CONNECTION_ESTABLISH_TIMEOUT_SEC, TimeUnit.SECONDS)
        val commandResult = connectionClient.executeAdbCommand(AdbCommand(command))
        return commandResult.toString()
    }

    private fun awaitConnectionEstablished(timeout: Long, timeUnit: TimeUnit) {
        val waitStepMs = 200L
        val timeoutMs = timeUnit.toMillis(timeout)
        var waitTime = 0L
        while (!connectionClient.isConnected() && waitTime <= timeoutMs) {
            Thread.sleep(waitStepMs)
            waitTime += waitStepMs
        }
        // todo throw some exception or message if connection has not been established
    }

    // todo inner or private?
    // todo logs
    // todo get name of Device?
    private class WatchdogThread : Thread("Connection watchdog thread from Device to Desktop") {
        override fun run() {
            logger.i(AdbConnection.javaClass.simpleName, "WatchdogThread start from Device to Desktop")
            while (isRunning.get() == true) {
                if (!connectionClient.isConnected()) {
                    // todo logs result of connection
                    runCatching {
                        logger.i(AdbConnection.javaClass.simpleName, "WatchdogThread. Try to connect..")
                        connectionClient.connect()
                    }
                }
                // todo sleep?
            }
        }
    }

}