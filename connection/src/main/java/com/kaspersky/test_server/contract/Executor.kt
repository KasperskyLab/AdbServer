package com.kaspersky.test_server.contract

// todo comments
interface Executor {

    fun <T> executeCommand(command: Command<T>)

}