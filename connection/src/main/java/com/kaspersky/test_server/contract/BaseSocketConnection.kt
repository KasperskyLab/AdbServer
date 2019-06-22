package com.kaspersky.test_server.contract

// todo think about
// todo 1. @Throws(IOException::class) for methods
// todo 2. comments
// todo 3. val isConnected: Boolean ?
interface BaseSocketConnection {

    fun connect()

    fun disconnect()

}