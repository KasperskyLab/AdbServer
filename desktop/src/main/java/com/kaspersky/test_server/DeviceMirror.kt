package com.kaspersky.test_server

import com.kaspersky.test_server.api.*
import com.kaspersky.test_server.cmd.CmdCommand
import com.kaspersky.test_server.cmd.CmdCommandExecutor
import com.kaspresky.test_server.log.Logger
import java.util.concurrent.atomic.AtomicReference

internal class DeviceMirror(
    val deviceName: String,
    private val logger: Logger
) {

    private lateinit var connectionServer: ConnectionServer
    private val isRunning = AtomicReference<Boolean>()

    fun startConnectionToDevice() {
        val desktopDeviceSocketConnection =
            DesktopDeviceSocketConnectionFactory.getSockets(
                DesktopDeviceSocketConnectionType.FORWARD,
                getExecutor(),
                logger
            )
        connectionServer = ConnectionFactory.getServer(
            desktopDeviceSocketConnection.getDesktopSocketLoad(),
            getExecutor(),
            logger
        )
        isRunning.set(true)
        WatchdogThread().start()
    }

    private fun getExecutor() = object : AdbCommandExecutor {
        override fun execute(command: AdbCommand): CommandResult {
            val cmdCommand = CmdCommand("adb -s $deviceName ${command.body}")
            return CmdCommandExecutor.execute(cmdCommand, logger)
        }

    }

    fun stopConnectionToDevice() {
        isRunning.set(false)
        connectionServer.disconnect()
    }

    // todo inner or private?
    // todo logs
    inner class WatchdogThread() : Thread("Connection watchdog thread from Desktop to Device = $deviceName") {
        override fun run() {
            while (isRunning.get() == true) {
                if (!connectionServer.isConnected()) {
                    // todo logs result of connection
                    runCatching { connectionServer.connect() }
                }
                sleep(500)
            }
        }
    }

}