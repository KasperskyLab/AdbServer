package com.kaspresky.test_server.log.filter_log

import com.kaspresky.test_server.log.Logger
import com.kaspresky.test_server.log.LoggerSystemImpl
import java.lang.Exception
import java.util.Deque
import java.util.ArrayDeque

internal class LoggerFilterSystemImpl(
    tag: String,
    deviceName: String? = null
) : Logger {

    private val logger: Logger = LoggerSystemImpl(tag, deviceName)
    private val systemLogger: Logger = LoggerSystemImpl(null, deviceName)

    private val logStack: Deque<LogData> = ArrayDeque()
    private var logRecorder: LogRecorder = LogRecorder()

    override fun i(text: String) {
        handleLog(text) { logger.i(text) }
    }

    override fun i(method: String, text: String) {
        handleLog(method + text) { logger.i(method, text) }
    }

    override fun d(text: String) {
        // todo maybe however distinguish i, d, e
        handleLog(text) { logger.d(text) }
    }

    override fun d(method: String, text: String) {
        handleLog(method + text) { logger.d(method, text) }
    }

    override fun e(exception: Exception) {
        handleLog(exception.localizedMessage) { logger.e(exception) }
    }

    override fun e(method: String, exception: Exception) {
        handleLog("${method}__${exception.localizedMessage}") { logger.e(method, exception) }
    }

    private fun handleLog(text: String, action: () -> Unit) {
        val logData = LogData(text, action)
        val position = logStack.indexOf(logData)
        val answer = logRecorder.put(position, LogData(text, action))
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
        fragmentStartString?.let { systemLogger.i(fragmentStartString) }
        readyRecord.recordingStack.descendingIterator().forEach { it.logOutput.invoke() }
        fragmentEndString?.let { systemLogger.i(fragmentEndString) }
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