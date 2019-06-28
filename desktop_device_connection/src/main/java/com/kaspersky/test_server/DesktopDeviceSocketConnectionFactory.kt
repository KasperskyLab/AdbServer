package com.kaspersky.test_server

import com.kaspersky.test_server.DesktopDeviceSocketConnectionType.*
import com.kaspresky.test_server.log.LoggerFactory

// todo think about singletons and a way to provide dependencies
object DesktopDeviceSocketConnectionFactory {

    private val logger = LoggerFactory.systemLogger()
    private val desktopDeviceSocketConnectionForwardImpl by
            lazy { DesktopDeviceSocketConnectionForwardImpl(logger) }

    fun getSockets(
        desktopDeviceSocketConnectionType: DesktopDeviceSocketConnectionType
    ): DesktopDeviceSocketConnection {
        return when(desktopDeviceSocketConnectionType) {
            FORWARD -> desktopDeviceSocketConnectionForwardImpl
            // todo correct exception
            REVERSE -> throw RuntimeException()
        }
    }

}