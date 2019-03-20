package com.kaspersky.test_server_command_handler.remote

import com.kaspersky.test_server_command_handler.ICommandExecutor
import java.io.IOException

interface IRemoteCommandExecutor : ICommandExecutor {

    val isConnected: Boolean

    @Throws(IOException::class)
    fun connect()

    @Throws(IOException::class)
    fun disconnect()
}