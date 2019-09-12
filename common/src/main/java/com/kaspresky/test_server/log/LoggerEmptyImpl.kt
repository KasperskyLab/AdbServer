package com.kaspresky.test_server.log

import java.lang.Exception

internal class LoggerEmptyImpl : Logger {

    override fun i(tag: String, text: String) = Unit

    override fun i(tag: String, method: String, text: String) = Unit

    override fun d(tag: String, text: String) = Unit

    override fun d(tag: String, method: String, text: String) = Unit

    override fun e(tag: String, exception: Exception) = Unit

    override fun e(tag: String, method: String, exception: Exception) = Unit
}