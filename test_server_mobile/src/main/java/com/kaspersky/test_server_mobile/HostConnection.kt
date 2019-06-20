package com.kaspersky.test_server_mobile

import com.kaspersky.test_server_command_handler.local.LocalCommandExecutor
import com.kaspersky.test_server_command_handler.remote.RemoteCommandExecutor
import com.kaspersky.test_server_contract.*
import java.io.IOException
import java.util.concurrent.TimeUnit


object HostConnection {
    private const val CONNECTION_ESTABLISH_TIMEOUT_SEC = 5L
    private val remoteCommandExecutor = RemoteCommandExecutor.server(DEVICE_PORT, LocalCommandExecutor())

    private var sWatchdogThread: WatchdogThread? = null

    @Synchronized
    fun start() {
        if (sWatchdogThread == null) {
            val thread = WatchdogThread(remoteCommandExecutor)
            thread.start()
            sWatchdogThread = thread
        }
    }

    @Synchronized
    fun stop() {
        sWatchdogThread?.disposed = true
        sWatchdogThread = null
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

private class WatchdogThread(val remoteCommandExecutor: RemoteCommandExecutor) : Thread("Host connection watchdog thread") {
    var disposed = false
    override fun run() {
        while (!disposed) {
            if (!remoteCommandExecutor.isConnected) {
                runCatching { remoteCommandExecutor.connect() }
            }
        }
    }
}