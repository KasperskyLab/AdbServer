package com.kaspersky.test_server.api

// todo think about
// todo 1. @Throws(IOException::class) for methods
// todo 2. comments
// todo 3. val isConnected: Boolean ?
interface BaseConnection {

    fun connect()

    fun disconnect()

    fun isConnected(): Boolean

}