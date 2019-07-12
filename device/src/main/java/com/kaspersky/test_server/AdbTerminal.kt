package com.kaspersky.test_server

import com.kaspersky.test_server.api.CommandResult
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

    /**
     * Please first of all call [connect] method to establish a connection
     */
    fun executeAdb(command: String): CommandResult = device.fulfill(AdbCommand(command))

    /**
     * Please first of all call [connect] method to establish a connection
     */
    fun executeCmd(command: String): CommandResult = device.fulfill(CmdCommand(command))

}