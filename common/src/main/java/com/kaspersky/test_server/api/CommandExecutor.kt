package com.kaspersky.test_server.api

/**
 * Executor of terminal commands.
 */
interface CommandExecutor {

    fun execute(command: Command): CommandResult

}