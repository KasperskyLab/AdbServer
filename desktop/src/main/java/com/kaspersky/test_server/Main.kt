package com.kaspersky.test_server

import com.kaspersky.test_server.cmd.CmdCommandExecutor
import com.kaspresky.test_server.log.LoggerFactory

internal fun main(args: Array<String>) {
    // handle args for debugger or forward/reverse ports?
    val logger = LoggerFactory.systemLogger()
    val cmdCommandExecutor = CmdCommandExecutor()
    val desktop = Desktop(cmdCommandExecutor, logger)
    desktop.startDevicesObserving()
}