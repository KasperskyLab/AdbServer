package com.kaspersky.test_server

import com.kaspersky.test_server.DesktopDeviceSocketConnectionType.*
import com.kaspresky.test_server.log.Logger

object DesktopDeviceSocketConnectionFactory {

    fun getSockets(
        desktopDeviceSocketConnectionType: DesktopDeviceSocketConnectionType,
        logger: Logger
    ): DesktopDeviceSocketConnection {
        return when(desktopDeviceSocketConnectionType) {
            FORWARD -> DesktopDeviceSocketConnectionForwardImpl(logger)
            REVERSE -> throw UnsupportedOperationException("Please implement REVERSE DesktopDeviceSocketConnection")
        }
    }

}