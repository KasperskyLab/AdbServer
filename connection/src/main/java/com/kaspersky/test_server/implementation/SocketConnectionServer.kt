package com.kaspersky.test_server.implementation

import com.kaspersky.test_server.contract.Command
import com.kaspersky.test_server.contract.Server
import com.kaspersky.test_server.contract.SocketConnection

class SocketConnectionServer : SocketConnection, Server {

    override fun connect() {

    }

    override fun disconnect() {

    }

    override fun <T> executeCommand(command: Command<T>) {

    }

}