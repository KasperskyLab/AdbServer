package com.kaspresky.test_server.log

internal class LoggerSystemImpl : Logger {

    companion object {
        private const val COMMON_TAG_LENGTH = 50
        private const val TAG = "tag="
        private const val MESSAGE = "message => "
    }

    override fun i(tag: String, text: String) {
        System.out.println("INFO:________$TAG${getTagString(tag)}$MESSAGE$text")
    }

    override fun d(tag: String, text: String) {
        System.out.println("DEBUG:_______$TAG${getTagString(tag)}$MESSAGE$text")
    }

    override fun e(tag: String, text: String) {
        System.out.println("ERROR:_______$TAG${getTagString(tag)}$MESSAGE$text")
    }

    private fun getTagString(tag: String): String =
        tag + "_".repeat(COMMON_TAG_LENGTH - tag.length)

}