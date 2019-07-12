package com.kaspersky.test_server

import com.kaspersky.test_server.api.ExecutorResultStatus
import com.kaspresky.test_server.log.Logger
import java.util.regex.Pattern

internal class Desktop(
    private val cmdCommandPerformer: CmdCommandPerformer,
    private val logger: Logger
) {

    private val tag = javaClass.simpleName
    private val devices: MutableCollection<DeviceMirror> = mutableListOf()

    fun startDevicesObserving() {
        logger.i(tag, "startDevicesObserving",  "start")
        while (true) {
            val namesOfAttachedDevicesByAdb = getAttachedDevicesByAdb()
            namesOfAttachedDevicesByAdb.forEach { deviceName ->
                if (devices.find { client -> client.deviceName == deviceName } == null) {
                    logger.i(
                        tag,
                        "startDevicesObserving",
                        "New device has been found: $deviceName. Initialize connection to it..."
                    )
                    val deviceMirror = DeviceMirror.create(
                        deviceName, cmdCommandPerformer, logger
                    )
                    deviceMirror.startConnectionToDevice()
                    devices += deviceMirror
                }
            }
            devices.removeIf { client ->
                if (client.deviceName !in namesOfAttachedDevicesByAdb) {
                    logger.i(
                        tag,
                        "startDevicesObserving",
                        "Adb connection to ${client.deviceName} has been missed. Stop connection."
                    )
                    client.stopConnectionToDevice()
                    return@removeIf true
                } else {
                    return@removeIf false
                }
            }
            Thread.sleep(500)
        }
    }

    private fun getAttachedDevicesByAdb(): List<String> {
        val pattern = Pattern.compile("^([a-zA-Z0-9\\-:.]+)(\\s+)(device)")
        val commandResult = cmdCommandPerformer.perform("adb devices")
        if (commandResult.status != ExecutorResultStatus.SUCCESS) {
            return emptyList()
        }
        val adbDevicesCommandResult: String = commandResult.description
        return adbDevicesCommandResult.lines()
            .asSequence()
            .map { pattern.matcher(it) }
            .filter { matcher -> matcher.find() }
            .map { matcher -> matcher.group(1) }
            .toList()
    }

}