package com.kaspersky.test_server.api

data class AdbCommandResult(
    val status: ExecutorResultStatus,
    val description: String
)

enum class ExecutorResultStatus {
    SUCCESS,
    FAILED
}