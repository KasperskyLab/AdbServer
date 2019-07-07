package com.kaspersky.test_server.api

/**
 * Interface for common Connection
 */
interface BaseConnection {

    fun tryConnect()

    fun tryDisconnect()

    fun isConnected(): Boolean

}