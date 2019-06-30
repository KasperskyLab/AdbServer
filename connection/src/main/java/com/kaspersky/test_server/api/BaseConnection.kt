package com.kaspersky.test_server.api

/**
 * Interface for common Connection
 */
interface BaseConnection {

    fun connect()

    fun disconnect()

    fun isConnected(): Boolean

}