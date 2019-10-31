package com.kaspresky.test_server.log.filter_log

import java.util.*

internal data class PutRecordAnswer(
    val answer: Answer,
    val recordedStack: Deque<LogData>? = null,
    val recordedFragmentsCount: Int? = null,
    val remainedStack: Deque<LogData>? = null
)

internal enum class Answer {
    NO_REPEAT,
    FRAGMENT_READY,
    FRAGMENT_IN_PROGRESS,
    ABORTED
}