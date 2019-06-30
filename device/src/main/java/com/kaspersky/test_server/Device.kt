package com.kaspersky.test_server

import com.kaspersky.test_server.api.*
import com.kaspresky.test_server.log.Logger
import com.kaspresky.test_server.log.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class Device private constructor(
    private val connectionClient: ConnectionClient,
    private val logger: Logger
) {

    companion object {
        private const val CONNECTION_ESTABLISH_TIMEOUT_SEC = 5L
        fun create(logger: Logger): Device {
            val desktopDeviceSocketConnection =
                DesktopDeviceSocketConnectionFactory.getSockets(
                    DesktopDeviceSocketConnectionType.FORWARD,
                    logger
                )
            val connectionClient = ConnectionFactory.createClient(
                desktopDeviceSocketConnection.getDeviceSocketLoad(),
                logger
            )
            return Device(connectionClient, logger)
        }
    }

    private val tag = javaClass.simpleName
    private val isRunning = AtomicReference<Boolean>()

    fun startConnectionToDesktop() {
        if (isRunning.compareAndSet(false, true)) {
            logger.i(tag, "start", "start")
            WatchdogThread().start()
        }
    }

    fun stopConnectionToDesktop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.i(tag, "stop", "stop")
            connectionClient.disconnect()
        }
    }

    /**
     * Please, be aware!
     * It's a synchronous and time-consuming method.
     * This method includes:
     * 1. a waiting time of the connection establishment (if it has not been yet)
     * 2. the adb command execution time
     */
    fun execute(command: String): String {
        logger.i(tag, "execute", "Start to execute the command=$command")
        val commandResult = try {
            awaitConnectionEstablished(CONNECTION_ESTABLISH_TIMEOUT_SEC, TimeUnit.SECONDS)
            connectionClient.executeAdbCommand(AdbCommand(command))
        } catch (exception: ConnectionTimeException) {
            CommandResult(
                ExecutorResultStatus.FAILED,
                "The time for the connection establishment is over"
            )
        }
        logger.i(tag, "execute", "The result of command=$command => $commandResult")
        return commandResult.toString()
    }

    @Throws(ConnectionTimeException::class)
    private fun awaitConnectionEstablished(timeout: Long, timeUnit: TimeUnit) {
        val waitStepMs = 200L
        val timeoutMs = timeUnit.toMillis(timeout)
        var waitTime = 0L
        while (!connectionClient.isConnected() && waitTime <= timeoutMs) {
            Thread.sleep(waitStepMs)
            waitTime += waitStepMs
        }
        if (!connectionClient.isConnected()) {
            throw ConnectionTimeException(
                "The time (timeout=$timeout, timeUnit=$timeUnit) for the connection establishment is over"
            )
        }
    }

    // todo get name of the device?
    private inner class WatchdogThread : Thread("Connection watchdog thread from Device to Desktop") {
        override fun run() {
            logger.i("$tag.WatchdogThread","run", "WatchdogThread starts from Device to Desktop")
            while (isRunning.get() == true) {
                if (!connectionClient.isConnected()) {
                    try {
                        logger.i("$tag.WatchdogThread", "run", "Try to connect to Desktop...")
                        connectionClient.connect()
                    } catch (exception: Exception) {
                        logger.i("$tag.WatchdogThread", "run", "The attempt to connect to Desktop was with exception: $exception")
                    }
                }
            }
        }
    }

}