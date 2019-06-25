package com.kaspersky.test_server.contract

interface CommandHandler<T> {

    // todo return value
    fun complete(command: Command): T

}