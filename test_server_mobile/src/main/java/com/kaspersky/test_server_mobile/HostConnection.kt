package com.kaspersky.test_server_mobile

import com.kaspersky.test_server_command_handler.local.LocalCommandExecutor
import com.kaspersky.test_server_command_handler.remote.RemoteCommandExecutor
import com.kaspersky.test_server_contract.*
import java.io.IOException
import java.util.concurrent.TimeUnit


object HostConnection {
    private const val CONNECTION_ESTABLISH_TIMEOUT_SEC = 5L
    private val remoteCommandExecutor = RemoteCommandExecutor.server(DEVICE_PORT, LocalCommandExecutor())

    private var watchdogThread: ConnectionWatchdogThread? = null

    @Synchronized
    fun start() {
        if (watchdogThread == null) {
            val thread = ConnectionWatchdogThread(remoteCommandExecutor)
            thread.start()
            watchdogThread = thread
        }
    }

    @Synchronized
    fun stop() {
        watchdogThread?.disposed = true
        watchdogThread = null
        remoteCommandExecutor.disconnect()
    }

    @Throws(
            AdbException::class,
            IOException::class
    )
    fun executeAdbCommand(adbCommand: String): String {
        awaitConnectionEstablished(CONNECTION_ESTABLISH_TIMEOUT_SEC, TimeUnit.SECONDS)
        try {
            return remoteCommandExecutor.execute(
                    AdbCommand(adbCommand)
            )
        } catch (e: IOException) {
            throw e
        } catch (e: AdbException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Unexpected error during adb command", e)
        }
    }

    @Throws(
            CmdException::class,
            IOException::class
    )
    fun executeCmdCommand(cmdCommand: String): String {
        awaitConnectionEstablished(CONNECTION_ESTABLISH_TIMEOUT_SEC, TimeUnit.SECONDS)
        try {
            return remoteCommandExecutor.execute(
                    CmdCommand(cmdCommand)
            )
        } catch (e: IOException) {
            throw e
        } catch (e: CmdException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Unexpected error during cmd command", e)
        }
    }

    private fun awaitConnectionEstablished(timeout: Long, timeUnit: TimeUnit) {
        val waitStepMs = 200L
        val timeoutMs = timeUnit.toMillis(timeout)
        var waitTime = 0L
        while (!remoteCommandExecutor.isConnected && waitTime <= timeoutMs) {
            Thread.sleep(waitStepMs)
            waitTime += waitStepMs
        }
    }
}

private class ConnectionWatchdogThread(val remoteCommandExecutor: RemoteCommandExecutor) : Thread("Host server watchdog thread") {
    var disposed = false
    override fun run() {
        while (!disposed) {
            if (!remoteCommandExecutor.isConnected) {
                runCatching { remoteCommandExecutor.connect() }
            }
        }
    }
}