package com.kaspersky.test_server.api

data class CommandResult(
    val status: ExecutorResultStatus,
    val description: String
)

enum class ExecutorResultStatus {
    SUCCESS,
    FAILED
}