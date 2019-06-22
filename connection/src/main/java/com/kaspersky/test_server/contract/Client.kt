package com.kaspersky.test_server.contract

// todo add comments
interface Client {

    fun <T> sendCommand(command: Command<T>)

}