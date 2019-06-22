package com.kaspresky.test_server.log

object LoggerFactory {

    fun emptyLogger(): Logger = LoggerEmptyImpl()

    fun systemLogger(): Logger = LoggerSystemImpl()

}