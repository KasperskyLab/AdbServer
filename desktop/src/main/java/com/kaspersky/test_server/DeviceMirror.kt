package com.kaspersky.test_server

import com.kaspersky.test_server.api.*
import com.kaspresky.test_server.log.Logger
import java.lang.Exception
import java.util.concurrent.atomic.AtomicReference

internal class DeviceMirror private constructor(
    val deviceName: String,
    private val connectionServer: ConnectionServer,
    private val logger: Logger
) {

    companion object {
        fun create(
            deviceName: String,
            cmdCommandPerformer: CmdCommandPerformer,
            logger: Logger
        ): DeviceMirror {
            val desktopDeviceSocketConnection =
                DesktopDeviceSocketConnectionFactory.getSockets(
                    DesktopDeviceSocketConnectionType.FORWARD,
                    logger
                )
            val commandExecutor = CommandExecutorImpl(
                cmdCommandPerformer, deviceName
            )
            val connectionServer = ConnectionFactory.createServer(
                desktopDeviceSocketConnection.getDesktopSocketLoad(commandExecutor),
                commandExecutor,
                logger
            )
            return DeviceMirror(deviceName, connectionServer, logger)
        }
    }

    private val tag = javaClass.simpleName
    private val isRunning = AtomicReference<Boolean>()

    fun startConnectionToDevice() {
        logger.i(tag, "startConnectionToDevice", "connect to device=$deviceName start")
        isRunning.set(true)
        WatchdogThread().start()
    }

    fun stopConnectionToDevice() {
        logger.i(tag, "stopConnectionToDevice", "connection to device=$deviceName was stopped")
        isRunning.set(false)
        connectionServer.tryDisconnect()
    }

    private inner class WatchdogThread : Thread() {
        override fun run() {
            logger.i("$tag.WatchdogThread", "run", "WatchdogThread is started from Desktop to Device=$deviceName")
            while (isRunning.get() == true) {
                if (!connectionServer.isConnected()) {
                    try {
                        logger.i("$tag.WatchdogThread", "run", "Try to connect to Device=$deviceName...")
                        connectionServer.tryConnect()
                    } catch (exception: Exception) {
                        logger.i("$tag.WatchdogThread", "run", "The attempt to connect to Device=$deviceName was with exception: $exception")
                    }
                }
                sleep(500)
            }
        }
    }

}