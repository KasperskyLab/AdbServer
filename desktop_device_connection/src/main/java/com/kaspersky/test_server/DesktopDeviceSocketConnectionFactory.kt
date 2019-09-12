package com.kaspersky.test_server

import com.kaspresky.test_server.log.Logger

object DesktopDeviceSocketConnectionFactory {

    fun getSockets(
        desktopDeviceSocketConnectionType: DesktopDeviceSocketConnectionType,
        logger: Logger
    ): DesktopDeviceSocketConnection {
        return when (desktopDeviceSocketConnectionType) {
            DesktopDeviceSocketConnectionType.FORWARD -> DesktopDeviceSocketConnectionForwardImpl(logger)
            DesktopDeviceSocketConnectionType.REVERSE -> throw UnsupportedOperationException("Please implement REVERSE DesktopDeviceSocketConnection")
        }
    }
}