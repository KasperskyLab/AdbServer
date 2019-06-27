package com.kaspersky.test_server.api

import com.kaspersky.test_server.implementation.ConnectionClientImplBySocket
import com.kaspersky.test_server.implementation.ConnectionServerImplBySocket
import com.kaspresky.test_server.log.Logger
import java.net.Socket

object ConnectionFactory {

    fun getServer(
        socket: Socket,
        adbCommandExecutor: AdbCommandExecutor,
        logger: Logger
    ): ConnectionServer =
            ConnectionServerImplBySocket(socket, adbCommandExecutor, logger)

    fun getClient(
        socket: Socket,
        logger: Logger
    ): ConnectionClient =
        ConnectionClientImplBySocket(socket, logger)

}