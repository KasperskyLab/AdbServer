package com.kaspersky.test_server.implementation

// todo logs, comments, exceptions
internal class ConnectionMaker {

    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    // todo synchronized
    fun connect(connectAction: () -> Unit) {
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCONNECTING) {
            // todo is it ok to throw exception?
            throw IllegalStateException("Unexpected connection state = [$connectionState] appeared during connect")
        }
        if (connectionState == ConnectionState.CONNECTED) {
            return
        }
        connectionState = ConnectionState.CONNECTING
        try {
            connectAction.invoke()
            connectionState = ConnectionState.CONNECTED
        } catch (exception: Exception) {
            connectionState = ConnectionState.DISCONNECTED
            throw exception
        }
    }

    // todo synchronized
    fun disconnect(connectAction: () -> Unit) {
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

