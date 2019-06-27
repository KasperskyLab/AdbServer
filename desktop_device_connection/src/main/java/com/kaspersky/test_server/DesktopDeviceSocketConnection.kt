package com.kaspersky.test_server

import com.kaspersky.test_server.api.AdbCommandExecutor
import java.net.Socket

// todo log that functions are maybe so long
interface DesktopDeviceSocketConnection {

    fun getDesktopSocketLoad(executor: AdbCommandExecutor): () -> Socket

    fun getDeviceSocketLoad(): () -> Socket

}