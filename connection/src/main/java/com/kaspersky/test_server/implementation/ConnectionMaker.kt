package com.kaspersky.test_server.implementation

import com.kaspresky.test_server.log.Logger

internal class ConnectionMaker(
    private val logger: Logger
) {

    private val tag = javaClass.simpleName
    @Volatile
    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    @Synchronized
    fun connect(connectAction: () -> Unit) {
        logger.i(tag, "connect", "start")
        logger.i(tag, "connect", "current state=$connectionState")
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            logger.i(tag, "connect", "Unexpected connection state appeared during connect")
            return
        }
        if (connectionState == ConnectionState.CONNECTED) {
            return
        }
        connectionState = ConnectionState.CONNECTING
        try {
            connectAction.invoke()
            connectionState = ConnectionState.CONNECTED
            logger.i(tag, "connect", "updated state=$connectionState")
        } catch (exception: Exception) {
            logger.e(tag, "connect", exception)
            connectionState = ConnectionState.DISCONNECTED
        }
    }

    @Synchronized
    fun disconnect(connectAction: () -> Unit) {
        logger.i(tag, "disconnect", "start")
        logger.i(tag, "disconnect", "current state=$connectionState")
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            logger.i(tag, "disconnect", "Unexpected connection state appeared during disconnect")
            return
        }
        if (connectionState == ConnectionState.DISCONNECTED) {
            return
        }
        try {
            connectionState = ConnectionState.DISCONNECTING
            logger.i(tag, "disconnect", "updated state=$connectionState")
            connectAction.invoke()
        } finally {
            connectionState = ConnectionState.DISCONNECTED
            logger.i(tag, "disconnect", "updated state=$connectionState")
        }
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

