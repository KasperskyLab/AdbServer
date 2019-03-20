package com.kaspersky.test_server_desktop

import com.kaspersky.test_server_command_handler.Command
import com.kaspersky.test_server_command_handler.ICommandHandler
import com.kaspersky.test_server_command_handler.local.LocalCommandExecutor
import com.kaspersky.test_server_command_handler.remote.DefaultLogger
import com.kaspersky.test_server_command_handler.remote.EmptyLogger
import com.kaspersky.test_server_command_handler.remote.RemoteCommandExecutor
import com.kaspersky.test_server_contract.*
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

private const val CMD_EXECUTION_TIMEOUT_MIN = 2L
private const val MIN_PORT_VALUE = 8600

private val deviceControllers: MutableCollection<DeviceConnectionController> = mutableListOf()
private val lastDevicePort = AtomicInteger(MIN_PORT_VALUE)
private var debugMode = false

private fun executeCmdCommand(cmdCommand: String, writeLogs: Boolean): String {
    if (writeLogs) {
        println("Execute command: $cmdCommand started")
    }
    val process = Runtime.getRuntime().exec(cmdCommand)
    val resultMsg = process.inputStream.bufferedReader().readText()
    process.waitFor(CMD_EXECUTION_TIMEOUT_MIN, TimeUnit.MINUTES)
    val exitCode = process.exitValue()
    if (exitCode != 0) {
        if (writeLogs) {
            println("Execute command: $cmdCommand error. Exit code: $exitCode, message: $resultMsg")
        }
        throw CmdException(resultMsg)
    } else {
        if (writeLogs) {
            println("Execute command: $cmdCommand success. Exit code: $exitCode, message: $resultMsg")
        }
        return resultMsg
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
            if (deviceControllers.find { controller -> controller.device == device } == null) {
                println("New device has been found: $device. Initialize connection to it...")
                val connectionController = DeviceConnectionController(getFreePort(), device)
                connectionController.init()
                deviceControllers += connectionController
            }
        }
        deviceControllers.removeIf { controller ->
            if (controller.device !in attachedDevices) {
                controller.dispose()
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

private class DeviceConnectionController(val port: Int, val device: String) : ICommandHandler {
    companion object {
        private const val IP = "127.0.0.1"
    }

    private val remoteCommandExecutor = RemoteCommandExecutor.client(
            IP,
            port,
            LocalCommandExecutor(this),
            logger = if (debugMode) {
                DefaultLogger(device)
            } else {
                EmptyLogger
            }
    )

    val isRunning = AtomicReference<Boolean?>()

    fun init() {
        if (isRunning.compareAndSet(null, true)) {
            var lastStateWasDisconnected = false
            Thread {
                executeAdbCommand("forward tcp:$port tcp:$DEVICE_PORT ", debugMode)
                while (isRunning.get() == true) {
                    if (!remoteCommandExecutor.isConnected) {
                        if (!lastStateWasDisconnected) {
                            println("Start connect retry to $device")
                            lastStateWasDisconnected = true
                        }
                        runCatching { remoteCommandExecutor.connect() }
                        if (remoteCommandExecutor.isConnected) {
                            lastStateWasDisconnected = false
                            println("Connected successfully to $device")
                        }
                    }
                    sleep(500)
                }
            }.start()
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

    private fun executeAdbCommand(adbCommand: String, writeLogs: Boolean): String {
        try {
            return if (!device.isEmpty()) {
                executeCmdCommand("adb -s $device $adbCommand", writeLogs)
            } else {
                executeCmdCommand("adb $adbCommand", writeLogs)
            }
        } catch (e: CmdException) {
            throw AdbException(e.message.orEmpty())
        }
    }

}
