package com.kaspersky.test_server_desktop

import com.kaspersky.test_server_command_handler.Command
import com.kaspersky.test_server_command_handler.ICommandHandler
import com.kaspersky.test_server_command_handler.local.LocalCommandExecutor
import com.kaspersky.test_server_command_handler.remote.DefaultLogger
import com.kaspersky.test_server_command_handler.remote.EmptyLogger
import com.kaspersky.test_server_command_handler.remote.Logger
import com.kaspersky.test_server_command_handler.remote.RemoteCommandExecutor
import com.kaspersky.test_server_contract.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

private const val EXECUTION_TIMEOUT_SECONDS = 2 * 60L
private const val MIN_PORT_VALUE = 9000

private val deviceClients: MutableCollection<DeviceClient> = mutableListOf()
private val lastDevicePort = AtomicInteger(MIN_PORT_VALUE)
private var debugMode = true

private fun log(message: String) {
    System.out.println("[${Calendar.getInstance().time}] $message")
}

@Throws(
        CmdException::class,
        IOException::class
)
private fun executeCmdCommand(cmdCommand: String,
                              logEnabled: Boolean = true,
                              logTag: String = ""): String {
    if (logEnabled) {
        log("$logTag: execute command '$cmdCommand' started")
    }
    val process = Runtime.getRuntime().exec(cmdCommand)
    if (process.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        val exitCode = process.exitValue()
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().readText()
            if (logEnabled) {
                log("$logTag: execute command '$cmdCommand' error. Exit code: $exitCode, message: $error")
            }
            throw CmdException(error)
        } else {
            val result = process.inputStream.bufferedReader().readText()
            if (logEnabled) {
                log("$logTag: execute command '$cmdCommand' success. Exit code: $exitCode, message: $result")
            }
            return result
        }
    } else {
        try {
            throw CmdException("Command execution timeout ($EXECUTION_TIMEOUT_SECONDS sec) overhead")
        } finally {
            process.destroy()
        }
    }

}

fun main(args: Array<String>) {
    if (args.size == 2) {
        if (args[0] == "debug") {
            debugMode = args[1].toBoolean()
        }
    }
    while (true) {
        val attachedDevices = attachedDevices()
        attachedDevices.forEach { device ->
            if (deviceClients.find { client -> client.device == device } == null) {
                log("New device has been found: $device. Initialize connection to it...")
                val client = DeviceClient(getFreePort(), device)
                client.init()
                deviceClients += client
            }
        }
        deviceClients.removeIf { client ->
            if (client.device !in attachedDevices) {
                client.dispose()
                return@removeIf true
            } else {
                return@removeIf false
            }
        }

        Thread.sleep(500)
    }
}

private fun getFreePort(): Int {
    return lastDevicePort.incrementAndGet()
}

private fun attachedDevices(): List<String> {
    val pattern = Pattern.compile("^([a-zA-Z0-9\\-:.]+)(\\s+)(device)")
    val devicesCmdOutput = executeCmdCommand("adb devices", debugMode)
    return devicesCmdOutput.lines()
            .asSequence()
            .map {
                val matcher = pattern.matcher(it)
                return@map if (matcher.find()) {
                    matcher.group(1)
                } else {
                    null
                }
            }
            .filterNotNull()
            .toList()
}

private class DeviceClient(val port: Int, val device: String) : ICommandHandler {
    companion object {
        private const val IP = "127.0.0.1"
    }

    private val remoteCommandExecutor = RemoteCommandExecutor.client(
            IP,
            port,
            LocalCommandExecutor(this),
            logger = remoteClientLogger()
    )

    private fun remoteClientLogger(): Logger {
        return if (debugMode) {
            DefaultLogger(device)
        } else {
            EmptyLogger
        }
    }

    val isRunning = AtomicReference<Boolean?>()

    fun init() {
        if (isRunning.compareAndSet(null, true)) {
            WatchdogThread().start()
        } else {
            throw IllegalStateException("Internal errors(Seems init called twice)")
        }
    }

    fun dispose() {
        isRunning.set(false)
    }

    override fun <T> accept(command: Command<T>) = when (command) {
        is CmdCommand -> true
        is AdbCommand -> true
        else -> false
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> execute(command: Command<*>): T {
        return when (command) {
            is CmdCommand -> executeCmdCommand(command.body, true) as T
            is AdbCommand -> executeAdbCommand(command.body, true) as T
            else -> throw IllegalStateException("Unexpected command : $command")
        }
    }

    private fun executeAdbCommand(adbCommand: String, logEnabled: Boolean): String {
        try {
            return executeCmdCommand("adb -s $device $adbCommand", logEnabled, device)
        } catch (e: CmdException) {
            throw AdbException(e.message.orEmpty())
        }
    }

    inner class WatchdogThread : Thread("Device connection watchdog thread. Device = $device") {
        override fun run() {
            var logRetry = false
            executeAdbCommand("forward tcp:$port tcp:$DEVICE_PORT ", debugMode)
            while (isRunning.get() == true) {
                if (!remoteCommandExecutor.isConnected) {
                    if (!logRetry) {
                        log("Start connect retry to $device")
                        logRetry = true
                    }
                    runCatching { remoteCommandExecutor.connect() }
                    if (remoteCommandExecutor.isConnected) {
                        logRetry = false
                        log("Connected successfully to $device")
                    }
                }
                sleep(500)
            }
        }
    }
}
