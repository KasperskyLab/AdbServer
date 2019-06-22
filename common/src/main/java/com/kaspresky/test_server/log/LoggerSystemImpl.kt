package com.kaspresky.test_server.log

internal class LoggerSystemImpl : Logger {

    override fun i(tag: String, text: String) {
        System.out.println("INFO:_______$tag:_____________$text")
    }

    override fun d(tag: String, text: String) {
        System.out.println("DEBUG:_______$tag:_____________$text")
    }

    override fun e(tag: String, text: String) {
        System.out.println("ERROR:_______$tag:_____________$text")
    }

}