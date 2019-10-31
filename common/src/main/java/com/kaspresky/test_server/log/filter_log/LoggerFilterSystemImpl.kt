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

    companion object {
        private const val NO_DATA: Int = -1
    }

    private val logger: Logger = LoggerSystemImpl(tag, deviceName)
    private val systemLogger: Logger = LoggerSystemImpl(null, deviceName)
    private val logStack: Deque<LogData> = ArrayDeque()
    private var logRecordFragment: LogRecordFragment? = null

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
        handleLog(method + exception.localizedMessage) { logger.e(method, exception) }
    }

    private fun handleLog(text: String, action: () -> Unit) {
        val logData = LogData(text, action)
        val position = logStack.indexOf(logData)
        if (position == NO_DATA) {
            logRecordFragment?.apply {
                logStack.clear()
                val putRecordAnswer = logRecordFragment?.stopAndGet()
                outputFragmentLogs(putRecordAnswer?.recordedStack, putRecordAnswer?.recordedFragmentsCount)
                putRecordAnswer?.recordedStack?.forEach {
                    logStack.addLast(it)
                }
                logRecordFragment = null
            }
            logStack.addLast(logData)
            action.invoke()
            return
        }
        if (logRecordFragment == null) {
            val size = logStack.size - position
            logRecordFragment = LogRecordFragment(size)
        }
        val positionFromEnd = logStack.size - position - 1
        val putRecordAnswer = logRecordFragment?.put(positionFromEnd, logData)
        when(putRecordAnswer?.answer) {
            Answer.FRAGMENT_IN_PROGRESS -> return
            Answer.FRAGMENT_READY -> {
                logStack.clear()
                outputFragmentLogs(putRecordAnswer.recordedStack, putRecordAnswer.recordedFragmentsCount)
                putRecordAnswer.recordedStack?.forEach {
                    logStack.addLast(it)
                }
                logRecordFragment = null
            }
            Answer.NO_REPEAT -> {
                // todo think about remain part of putRecordAnswer.recordedStack before NO_REPEAT
                logStack.clear()
                outputFragmentLogs(putRecordAnswer.recordedStack, putRecordAnswer.recordedFragmentsCount)
                putRecordAnswer.recordedStack?.forEach {
                    logStack.addLast(it)
                }
                logRecordFragment = null
                logStack.addLast(logData)
                action.invoke()
            }
            Answer.ABORTED -> { }
        }

    }

    private fun outputFragmentLogs(stack: Deque<LogData>?, recordedFragmentsCount: Int?) {
        if (stack == null || stack.isEmpty()) {
            return
        }
        var fragmentStartString: String? = null
        var fragmentEndString: String? = null
        if (recordedFragmentsCount != null && recordedFragmentsCount > 1) {
            fragmentStartString = "/".repeat(40) +
                    "FRAGMENT IS REPEATED $recordedFragmentsCount TIMES" +
                    "/".repeat(40)
            fragmentEndString = "/".repeat(100)
        }
        //
        fragmentStartString?.let { systemLogger.i(fragmentStartString) }
        stack.forEach { it.logOutput.invoke() }
        fragmentEndString?.let { systemLogger.i(fragmentEndString) }
    }
}