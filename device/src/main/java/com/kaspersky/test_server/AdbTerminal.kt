package com.kaspersky.test_server

import com.kaspresky.test_server.log.LoggerFactory

object AdbTerminal {

    private val device = Device.create(
        LoggerFactory.systemLogger()
    )

    fun connect() {
        device.startConnectionToDesktop()
    }

    fun disconnect() {
        device.stopConnectionToDesktop()
    }

    fun executeAdb(command: String): String = device.fulfill(AdbCommand(command))

    fun executeCmd(command: String): String = device.fulfill(CmdCommand(command))

}