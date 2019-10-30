package com.kaspresky.test_server.log

import java.lang.Exception

internal class LoggerEmptyImpl : Logger {

    override fun i(text: String) = Unit

    override fun i(method: String, text: String) = Unit

    override fun d(text: String) = Unit

    override fun d(method: String, text: String) = Unit

    override fun e(exception: Exception) = Unit

    override fun e(method: String, exception: Exception) = Unit
}