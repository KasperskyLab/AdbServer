package com.kaspersky.test_server.cmd

import com.kaspersky.test_server.api.CommandResult
import com.kaspersky.test_server.api.ExecutorResultStatus
import com.kaspresky.test_server.log.Logger
import java.util.concurrent.TimeUnit

// logs
internal object CmdCommandExecutor {

    private const val EXECUTION_TIMEOUT_SECONDS = 2 * 60L

    fun execute(command: CmdCommand, logger: Logger): CommandResult {
//        logger.i(javaClass.simpleName, "execute(command=$command) start")
        val process = Runtime.getRuntime().exec(command.body)
        if (process.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            val exitCode = process.exitValue()
            val commandResult: CommandResult
            commandResult = if (exitCode != 0) {
                val error = "exitCode=$exitCode, message=${process.errorStream.bufferedReader().readText()}"
                CommandResult(ExecutorResultStatus.FAILED, error)
            } else {
                val success = "exitCode=$exitCode, message=${process.inputStream.bufferedReader().readText()}"
                CommandResult(ExecutorResultStatus.SUCCESS, success)
            }
//            logger.i(javaClass.simpleName, "execute(command=$command) with result=$commandResult")
            return commandResult
        }
        try {
            val commandResult = CommandResult(ExecutorResultStatus.FAILED, "Command execution timeout ($EXECUTION_TIMEOUT_SECONDS sec) overhead")
//            logger.i(javaClass.simpleName, "execute(command=$command) with result=$commandResult")
            return commandResult
        } finally {
            process.destroy()
        }
    }

}