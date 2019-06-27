package com.kaspersky.test_server

import com.kaspersky.test_server.api.AdbCommandExecutor
import com.kaspresky.test_server.log.Logger
import com.kaspersky.test_server.DesktopDeviceSocketConnectionType.*

object DesktopDeviceSocketConnectionFactory {

    fun getSockets(
        desktopDeviceSocketConnectionType: DesktopDeviceSocketConnectionType,
        logger: Logger
    ): DesktopDeviceSocketConnection {
        return when(desktopDeviceSocketConnectionType) {
            FORWARD -> DesktopDeviceSocketConnectionForwardImpl(logger)
            REVERSE -> throw RuntimeException()
        }
    }

}