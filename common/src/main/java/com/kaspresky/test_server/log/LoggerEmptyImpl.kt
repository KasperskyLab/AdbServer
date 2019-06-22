package com.kaspresky.test_server.log

internal class LoggerEmptyImpl : Logger {

    override fun i(tag: String, text: String) = Unit

    override fun d(tag: String, text: String) = Unit

    override fun e(tag: String, text: String) = Unit

}