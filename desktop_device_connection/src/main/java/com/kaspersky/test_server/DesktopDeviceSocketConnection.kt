package com.kaspersky.test_server

import com.kaspersky.test_server.api.AdbCommandExecutor
import java.net.Socket

/**
 * Please use only this interface to provide sockets for your connection between Desktop and Device
 *
 * DesktopDeviceSocketConnection provides:
 * 1. A pair of sockets according to correct setting of ports and correct forwarding of ports for Desktop and Device
 * 2. A lambda to load the socket because it may be time consuming process
 */
interface DesktopDeviceSocketConnection {

    fun getDesktopSocketLoad(executor: AdbCommandExecutor): () -> Socket

    fun getDeviceSocketLoad(): () -> Socket

}