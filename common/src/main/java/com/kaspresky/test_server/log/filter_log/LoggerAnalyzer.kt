package com.kaspresky.test_server.log.filter_log

import com.kaspresky.test_server.log.full_logger.FullLogger
import java.util.Deque
import java.util.ArrayDeque

internal class LoggerAnalyzer(
    private val fullLogger: FullLogger
) : FullLogger {

    private val logStack: Deque<LogData> = ArrayDeque()
    private var logRecorder: LogRecorder = LogRecorder()

    override fun log(
        logType: FullLogger.LogType?,
        deviceName: String?,
        tag: String?,
        method: String?,
        text: String?
    ) {
        handleLog(
            key = "$logType$deviceName$tag$method$text",
            action = { fullLogger.log(logType, deviceName, tag, method, text) }
        )
    }

    private fun handleLog(key: String, action: () -> Unit) {
        val logData = LogData(key, action)
        val position = logStack.indexOf(logData)
        val answer = logRecorder.put(position, LogData(key, action))
        when(answer) {
            is RecordInProgress -> { return }
            is ReadyRecord -> {
                outputRecord(answer)
                updateState(answer)
            }
        }
    }

    private fun outputRecord(readyRecord: ReadyRecord) {
        // prepare the first and the last log for recorded Fragment if it's needed
        var fragmentStartString: String? = null
        var fragmentEndString: String? = null
        if (readyRecord.countOfRecordingStack > 0) {
            fragmentStartString = "/".repeat(40) +
                    "FRAGMENT IS REPEATED ${readyRecord.countOfRecordingStack} TIMES" +
                    "/".repeat(40)
            fragmentEndString = "/".repeat(100)
        }
        // output record
        fragmentStartString?.let { fullLogger.log(text = fragmentStartString) }
        readyRecord.recordingStack.descendingIterator().forEach { it.logOutput.invoke() }
        fragmentEndString?.let { fullLogger.log(text = fragmentEndString) }
        // output remained part
        readyRecord.remainedStack.descendingIterator().forEach { it.logOutput.invoke() }
    }

    private fun updateState(readyRecord: ReadyRecord) {
        if (readyRecord.recordingStack.isNotEmpty()) {
            logStack.clear()
            readyRecord.recordingStack.forEach {
                logStack.addLast(it)
            }
        }
        readyRecord.remainedStack.descendingIterator().forEach {
            logStack.addFirst(it)
        }
    }
}