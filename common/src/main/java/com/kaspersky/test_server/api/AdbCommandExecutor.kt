package com.kaspersky.test_server.api

/**
 * High-level interface.
 * Executor of ADB commands.
 */
interface AdbCommandExecutor {

    fun execute(command: AdbCommand): CommandResult

}