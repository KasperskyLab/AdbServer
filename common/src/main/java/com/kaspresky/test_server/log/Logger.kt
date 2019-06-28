package com.kaspresky.test_server.log

/**
 * Common interface to log all actions
 */
interface Logger {

    /**
     * Info level of logging with tag.
     */
    fun i(tag: String, text: String)

    /**
     * Info level of logging with tag.
     */
    fun i(tag: String, method: String, text: String)

    /**
     * Debug level of logging with tag.
     */
    fun d(tag: String, text: String)

    /**
     * Debug level of logging with tag.
     */
    fun d(tag: String, method: String, text: String)

    /**
     * Error level of logging with tag.
     */
    fun e(tag: String, text: String)

    /**
     * Error level of logging with tag.
     */
    fun e(tag: String, method: String, text: String)

}