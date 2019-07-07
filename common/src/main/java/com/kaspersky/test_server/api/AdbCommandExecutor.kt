package com.kaspersky.test_server.api

/**
 * Executor of Adb commands.
 */
interface AdbCommandExecutor {

    fun execute(command: AdbCommand): CommandResult

}