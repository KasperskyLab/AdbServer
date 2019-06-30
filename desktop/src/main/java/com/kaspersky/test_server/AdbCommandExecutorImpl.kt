package com.kaspersky.test_server

import com.kaspersky.test_server.api.AdbCommand
import com.kaspersky.test_server.api.AdbCommandExecutor
import com.kaspersky.test_server.api.CommandResult
import com.kaspersky.test_server.cmd.CmdCommand
import com.kaspersky.test_server.cmd.CmdCommandExecutor
import com.kaspresky.test_server.log.Logger

internal class AdbCommandExecutorImpl(
    private val cmdCommandExecutor: CmdCommandExecutor,
    private val deviceName: String,
    private val logger: Logger
) : AdbCommandExecutor {

    override fun execute(command: AdbCommand): CommandResult {
        val cmdCommand = CmdCommand("adb -s $deviceName ${command.body}")
        return cmdCommandExecutor.execute(cmdCommand, logger)
    }

}