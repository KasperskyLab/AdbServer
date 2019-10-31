package com.kaspresky.test_server.log

import java.lang.Exception

/**
 * Presents logs in next form:
 * INFO:_____tag=ConnectionMaker_________________________method=connect()_______________________________message => start
 * INFO:_____tag=ConnectionMaker_________________________method=connect()_______________________________message => current state=DISCONNECTED
 */
internal class LoggerSystemImpl(
    private val tag: String?,
    private val deviceName: String? = null
) : Logger {

    companion object {
        private const val COMMON_FIELD_LENGTH = 40
        private const val TAG = "tag="
        private const val METHOD = "method="
        private const val MESSAGE = "message => "
    }

    override fun i(text: String) {
        println("INFO:_____${getDevice()}${getTag()}${getShortMessage(text)}")
    }

    override fun i(method: String, text: String) {
        println("INFO:_____${getDevice()}${getTag()}${getLongMessage(method, text)}")
    }

    override fun d(text: String) {
        println("DEBUG:____${getDevice()}${getTag()}${getShortMessage(text)}")
    }

    override fun d(method: String, text: String) {
        println("DEBUG:____${getDevice()}${getTag()}${getLongMessage(method, text)}")
    }

    override fun e(exception: Exception) {
        System.err.println("ERROR:____${getDevice()}${getTag()}${getShortMessage(exception)}")
    }

    override fun e(method: String, exception: Exception) {
        System.err.println("ERROR:____${getDevice()}${getTag()}${getLongMessage(method, exception)}")
    }

    private fun getDevice(): String =
        if (deviceName != null) { "device=${deviceName}____" } else { "" }

    private fun getShortMessage(message: Any): String =
        "$MESSAGE$message"

    private fun getLongMessage(method: String, message: Any) =
        "$METHOD${getFieldString(method)}$MESSAGE$message"

    private fun getTag(): String =
        if (tag != null) { "$TAG${getFieldString(tag)}" } else ""

    private fun getFieldString(text: String): String {
        if (text.length >= COMMON_FIELD_LENGTH) {
            return text + "_"
        }
        return text + "_".repeat(COMMON_FIELD_LENGTH - text.length)
    }
}