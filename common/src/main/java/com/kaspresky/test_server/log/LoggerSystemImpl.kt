package com.kaspresky.test_server.log

import java.lang.Exception

/**
 * Presents logs in next form:
 * INFO:_____tag=ConnectionMaker_________________________method=connect()_______________________________message => start
 * INFO:_____tag=ConnectionMaker_________________________method=connect()_______________________________message => current state=DISCONNECTED
 */
internal class LoggerSystemImpl : Logger {

    companion object {
        private const val COMMON_FIELD_LENGTH = 40
        private const val TAG = "tag="
        private const val METHOD = "method="
        private const val MESSAGE = "message => "
    }

    override fun i(tag: String, text: String) {
        System.out.println("INFO:_____${getShortMessage(tag, text)}")
    }

    override fun i(tag: String, method: String, text: String) {
        System.out.println("INFO:_____${getLongMessage(tag, method, text)}")
    }

    override fun d(tag: String, text: String) {
        System.out.println("DEBUG:____${getShortMessage(tag, text)}")
    }

    override fun d(tag: String, method: String, text: String) {
        System.out.println("DEBUG:____${getLongMessage(tag, method, text)}")
    }

    override fun e(tag: String, exception: Exception) {
        System.err.println("ERROR:____${getShortMessage(tag, exception)}")
    }

    override fun e(tag: String, method: String, exception: Exception) {
        System.err.println("ERROR:____${getLongMessage(tag, method, exception)}")
    }

    private fun getShortMessage(tag: String, message: Any): String =
        "$TAG${getFieldString(tag)}$MESSAGE$message"

    private fun getLongMessage(tag: String, method: String, message: Any) =
        "$TAG${getFieldString(tag)}$METHOD${getFieldString(method)}$MESSAGE$message"

    private fun getFieldString(text: String): String {
        if (text.length > COMMON_FIELD_LENGTH) {
            return text + "_"
        }
        return text + "_".repeat(COMMON_FIELD_LENGTH - text.length)
    }
}