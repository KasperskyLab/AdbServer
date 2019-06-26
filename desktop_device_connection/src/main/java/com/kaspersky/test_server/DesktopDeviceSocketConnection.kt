package com.kaspersky.test_server

import java.net.Socket

interface DesktopDeviceSocketConnection {

    fun getDesktopSocket(): () -> Socket

    fun getDeviceSocket(): () -> Socket

}