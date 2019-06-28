package com.kaspersky.test_server

import com.kaspersky.test_server.api.ExecutorResultStatus
import com.kaspersky.test_server.cmd.CmdCommand
import com.kaspersky.test_server.cmd.CmdCommandExecutor
import com.kaspresky.test_server.log.Logger
import java.util.*
import java.util.regex.Pattern

// todo logs
internal class Desktop(
    private val logger: Logger
) {

    private val devices: MutableCollection<DeviceMirror> = mutableListOf()

    fun startDevicesObserving() {
        logger.i(javaClass.simpleName, "startDevicesObserving() start")
        while (true) {
            val namesOfAttachedDevicesByAdb = getAttachedDevicesByAdb()
            namesOfAttachedDevicesByAdb.forEach { deviceName ->
                if (devices.find { client -> client.deviceName == deviceName } == null) {
                    // todo success log
                    // log("New device has been found: $device. Initialize connection to it...")
                    val deviceMirror = DeviceMirror(deviceName, logger)
                    deviceMirror.startConnectionToDevice()
                    devices += deviceMirror
                }
            }
            devices.removeIf { client ->
                if (client.deviceName !in namesOfAttachedDevicesByAdb) {
                    // todo logs
                    client.stopConnectionToDevice()
                    return@removeIf true
                } else {
                    return@removeIf false
                }
            }
            // todo maybe not sleep and replace from main thread?
            Thread.sleep(500)
        }
    }

    private fun getAttachedDevicesByAdb(): List<String> {
        val pattern = Pattern.compile("^([a-zA-Z0-9\\-:.]+)(\\s+)(device)")
        val commandResult = CmdCommandExecutor.execute(
            CmdCommand("adb devices"), logger
        )
        if (commandResult.status != ExecutorResultStatus.SUCCESS) {
            return Collections.emptyList()
        }
        val commandResultDescription: String = commandResult.description
        return commandResultDescription.lines()
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

}