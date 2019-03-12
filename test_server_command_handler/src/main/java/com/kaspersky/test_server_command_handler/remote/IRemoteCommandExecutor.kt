package com.kaspersky.test_server_command_handler.remote

import com.kaspersky.test_server_command_handler.ICommandExecutor
import java.io.IOException

interface IRemoteCommandExecutor : ICommandExecutor {
    @Throws(IOException::class)
    fun connect()

    fun disconnect()

    fun isConnected(): Boolean
}