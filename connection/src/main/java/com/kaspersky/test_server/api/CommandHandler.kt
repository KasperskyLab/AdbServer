package com.kaspersky.test_server.api

interface CommandHandler<T> {

    // todo return value
    fun complete(command: Command): T

}