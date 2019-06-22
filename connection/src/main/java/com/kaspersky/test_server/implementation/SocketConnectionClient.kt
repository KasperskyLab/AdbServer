package com.kaspersky.test_server.implementation

import com.kaspersky.test_server.contract.Client
import com.kaspersky.test_server.contract.Command
import com.kaspersky.test_server.contract.SocketConnection

class SocketConnectionClient : SocketConnection, Client {

    override fun connect() {

    }

    override fun disconnect() {

    }

    override fun <T> sendCommand(command: Command<T>) {

    }

}