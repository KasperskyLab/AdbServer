package com.kaspresky.test_server.log.filter_log

import java.util.*

internal class LogRecordFragment(
    private val size: Int
) {

    private var currentPosition: Int = size - 1
    private val recordLogStack: Deque<LogData> = ArrayDeque()
    private var recordedFragmentsCount: Int = 0

    fun put(positionFromEnd: Int, logData: LogData): PutRecordAnswer {
        if (positionFromEnd != currentPosition) {
            // todo throw exception and require to recreate class
            // todo
            val remainedFragmentStack: Deque<LogData> = ArrayDeque()
            return PutRecordAnswer(Answer.NO_REPEAT, ArrayDeque(recordLogStack), recordedFragmentsCount)
        }
        if (recordedFragmentsCount == 0) {
            recordLogStack.addLast(logData)
        }
        currentPosition--
        if (currentPosition < 0) {
            currentPosition = size - 1
            recordedFragmentsCount++
        }
        return PutRecordAnswer(Answer.FRAGMENT_IN_PROGRESS)
    }

    fun stopAndGet(): PutRecordAnswer {
        // todo
        return PutRecordAnswer(Answer.ABORTED, recordLogStack, recordedFragmentsCount)
    }

}