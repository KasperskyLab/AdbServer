package com.kaspersky.test_server

import com.kaspresky.test_server.log.LoggerFactory

internal fun main(args: Array<String>) {
    // handle args for debugger or forward/reverse ports?
    val logger = LoggerFactory.systemLogger()
    val cmdCommandPerformer = CmdCommandPerformer()
    val desktop = Desktop(cmdCommandPerformer, logger)
    desktop.startDevicesObserving()
}