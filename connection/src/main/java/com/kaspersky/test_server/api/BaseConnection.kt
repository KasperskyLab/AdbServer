package com.kaspersky.test_server.api

// todo think about
// todo 1. @Throws(IOException::class) for methods
// todo 2. comments
interface BaseConnection {

    fun connect()

    fun disconnect()

    fun isConnected(): Boolean

}