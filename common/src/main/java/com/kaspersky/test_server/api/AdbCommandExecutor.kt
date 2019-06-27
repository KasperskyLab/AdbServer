package com.kaspersky.test_server.api

// todo comments
// todo make a wrapper up Result
interface AdbCommandExecutor {

    // todo return value?
    fun execute(command: AdbCommand): CommandResult

}