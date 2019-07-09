package com.kaspersky.test_server.api

/**
 * BaseConnection + opportunity to execute Adb commands
 */
interface ConnectionClient : BaseConnection {

    /**
     * It is synchronous method to not reorder a line of adb commands
     * because if adb commands were completed in incorrect order it may to lead inconsistent state of the app and the device
     */
    fun executeCommand(command: Command): CommandResult

}