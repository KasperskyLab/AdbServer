package com.kaspresky.test_server.log

interface Logger {

    /**
     * Info level of logging with tag.
     */
    fun i(tag: String, text: String)

    /**
     * Debug level of logging with tag.
     */
    fun d(tag: String, text: String)

    /**
     * Error level of logging with tag.
     */
    fun e(tag: String, text: String)

}