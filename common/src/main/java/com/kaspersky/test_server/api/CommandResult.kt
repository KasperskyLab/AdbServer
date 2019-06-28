package com.kaspersky.test_server.api

import java.io.Serializable

data class CommandResult(
    val status: ExecutorResultStatus,
    val description: String
) : Serializable

enum class ExecutorResultStatus {
    SUCCESS,
    FAILED
}