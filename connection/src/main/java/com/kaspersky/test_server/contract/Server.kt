package com.kaspersky.test_server.contract

// todo add comments
// todo think about returned value
interface Server {

    fun <T> executeCommand(command: Command<T>)

}