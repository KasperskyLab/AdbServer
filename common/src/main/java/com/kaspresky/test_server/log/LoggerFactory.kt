package com.kaspresky.test_server.log

/**
 * The singleton to provide Logger interface and to hide an implementation
 */
object LoggerFactory {

    fun emptyLogger(): Logger = LoggerEmptyImpl()

    fun systemLogger(): Logger = LoggerSystemImpl()
}