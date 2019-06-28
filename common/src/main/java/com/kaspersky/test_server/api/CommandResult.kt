package com.kaspersky.test_server.api

import java.io.Serializable

data class CommandResult(
    val status: ExecutorResultStatus,
    val description: String
) : Serializable

enum class ExecutorResultStatus {
    // if the command was delivered to a server and the response was received from a server
    SUCCESS,
    // if something went wrong along the server-client road
    FAILED
}