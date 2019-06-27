package com.kaspersky.test_server.api

import com.kaspersky.test_server.implementation.ConnectionClientImplBySocket
import com.kaspersky.test_server.implementation.ConnectionServerImplBySocket
import com.kaspresky.test_server.log.Logger
import java.net.Socket

object ConnectionFactory {

    fun getServer(
        socketCreation: () -> Socket,
        adbCommandExecutor: AdbCommandExecutor,
        logger: Logger
    ): ConnectionServer =
            ConnectionServerImplBySocket(socketCreation, adbCommandExecutor, logger)

    fun getClient(
        socketCreation: () -> Socket,
        logger: Logger
    ): ConnectionClient =
        ConnectionClientImplBySocket(socketCreation, logger)

}