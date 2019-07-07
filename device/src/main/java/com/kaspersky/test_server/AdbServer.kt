package com.kaspersky.test_server

import com.kaspresky.test_server.log.LoggerFactory

object AdbServer {

    private val device = Device.create(
        LoggerFactory.systemLogger()
    )

    fun connect() {
        device.startConnectionToDesktop()
    }

    fun disconnect() {
        device.stopConnectionToDesktop()
    }

    fun execute(command: String): String = device.execute(command)

}