package com.kaspersky.test_server

import java.net.Socket

// todo log that functions are maybe so long
interface DesktopDeviceSocketConnection {

    fun getDesktopSocketLoad(): () -> Socket

    fun getDeviceSocketLoad(): () -> Socket

}