package com.kaspersky.test_server.api

interface ConnectionClient : BaseConnection {

    fun executeAdbCommand(command: AdbCommand): CommandResult

}