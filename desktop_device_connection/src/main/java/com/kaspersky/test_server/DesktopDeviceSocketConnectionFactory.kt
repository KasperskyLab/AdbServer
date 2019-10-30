package com.kaspersky.test_server

object DesktopDeviceSocketConnectionFactory {

    fun getSockets(
        desktopDeviceSocketConnectionType: DesktopDeviceSocketConnectionType,
        deviceName: String
    ): DesktopDeviceSocketConnection {
        return when (desktopDeviceSocketConnectionType) {
            DesktopDeviceSocketConnectionType.FORWARD -> DesktopDeviceSocketConnectionForwardImpl(deviceName)
            DesktopDeviceSocketConnectionType.REVERSE -> throw UnsupportedOperationException("Please implement REVERSE DesktopDeviceSocketConnection")
        }
    }
}