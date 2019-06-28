package com.kaspersky.test_server.api

// todo comments
interface AdbCommandExecutor {

    fun execute(command: AdbCommand): CommandResult

}