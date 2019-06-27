package com.kaspersky.test_server.api

data class ExecutorResult(
    val status: ExecutorResultStatus,
    val description: String
)

enum class ExecutorResultStatus {
    SUCCESS,
    FAILED
}