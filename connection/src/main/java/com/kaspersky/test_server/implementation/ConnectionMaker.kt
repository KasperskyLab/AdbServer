package com.kaspersky.test_server.implementation

import com.kaspresky.test_server.log.Logger

// todo logs, comments, exceptions
internal class ConnectionMaker(
    private val logger: Logger
) {

    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    // todo synchronized
    fun connect(connectAction: () -> Unit) {
        logger.i(javaClass.simpleName, "connect() start")
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            // todo is it ok to throw exception?
            throw IllegalStateException("Unexpected connection state = [$connectionState] appeared during connect")
        }
        if (connectionState == ConnectionState.CONNECTED) {
            logger.i(javaClass.simpleName, "connect() => already CONNECTED")
            return
        }
        connectionState = ConnectionState.CONNECTING
        try {
            connectAction.invoke()
            connectionState = ConnectionState.CONNECTED
            logger.i(javaClass.simpleName, "connect() => CONNECTED")
        } catch (exception: Exception) {
            connectionState = ConnectionState.DISCONNECTED
            throw exception
        }
        logger.i(javaClass.simpleName, "connect() completed")
    }

    // todo synchronized
    fun disconnect(connectAction: () -> Unit) {
        logger.i(javaClass.simpleName, "disconnect() start")
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            // todo is it ok to throw exception?
            throw IllegalStateException("Unexpected connection state = [$connectionState] appeared during disconnect")
        }
        if (connectionState == ConnectionState.DISCONNECTED) {
            return
        }
        try {
            connectionState = ConnectionState.DISCONNECTING
            connectAction.invoke()
        } finally {
            connectionState = ConnectionState.DISCONNECTED
            logger.i(javaClass.simpleName, "connect() => DISCONNECTED")
        }
        logger.i(javaClass.simpleName, "disconnect() completed")
    }

    fun isConnected(): Boolean =
        connectionState == ConnectionState.CONNECTED

    private enum class ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

}

