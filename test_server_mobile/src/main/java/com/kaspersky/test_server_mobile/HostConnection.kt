package com.kaspersky.test_server_mobile

import com.kaspersky.test_server_command_handler.local.LocalCommandExecutor
import com.kaspersky.test_server_command_handler.remote.RemoteCommandExecutor
import com.kaspersky.test_server_contract.CmdCommand
import com.kaspersky.test_server_contract.CmdException
import com.kaspersky.test_server_contract.PORT
import java.io.IOException
import java.util.concurrent.Executors

object HostConnection {
    private val remoteCommandExecutor =
        RemoteCommandExecutor.server(PORT, LocalCommandExecutor())
    private val executor = Executors.newSingleThreadExecutor()


    fun startAsync() {
        executor.execute { start() }
    }

    fun start() {
        remoteCommandExecutor.connect()
    }

    fun stop() {
        remoteCommandExecutor.disconnect()
    }

    @Throws(
        CmdException::class,
        IOException::class
    )
    fun executeAdbCommand(adbCommand: String): String {
        try {
            return remoteCommandExecutor.execute(
                CmdCommand(
                    "adb $adbCommand"
                )
            )
        } catch (e: IOException) {
            throw e
        } catch (e: CmdException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Unexpected error during adb command", e)
        }
    }

    @Throws(
        CmdException::class,
        IOException::class
    )
    fun executeCmdCommand(cmdCommand: String): String {
        try {
            return remoteCommandExecutor.execute(
                CmdCommand(
                    cmdCommand
                )
            )
        } catch (e: IOException) {
            throw e
        } catch (e: CmdException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Unexpected error during cmd command", e)
        }
    }
}