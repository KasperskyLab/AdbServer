package com.kaspersky.test_server

import com.kaspersky.test_server.api.Command
import com.kaspersky.test_server.api.CommandExecutor
import com.kaspersky.test_server.api.CommandResult
import java.lang.UnsupportedOperationException

internal class CommandExecutorImpl(
    private val cmdCommandPerformer: CmdCommandPerformer,
    private val deviceName: String
) : CommandExecutor {

    override fun execute(command: Command): CommandResult {
        return when (command) {
            is CmdCommand -> cmdCommandPerformer.perform(command.body)
            is AdbCommand -> cmdCommandPerformer.perform("adb -s $deviceName ${command.body}")
            else -> throw UnsupportedOperationException("The command=$command is unsupported command")
        }
    }

}