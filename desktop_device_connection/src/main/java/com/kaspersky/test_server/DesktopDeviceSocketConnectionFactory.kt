package com.kaspersky.test_server

import com.kaspresky.test_server.log.Logger
import com.kaspersky.test_server.DesktopDeviceSocketConnectionType.*

// todo think about singletons and a way to provide dependencies
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