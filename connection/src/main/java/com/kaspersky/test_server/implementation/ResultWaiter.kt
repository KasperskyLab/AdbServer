package com.kaspersky.test_server.implementation

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class ResultWaiter<Result> {

    @Volatile
    private var result: Result? = null
    private val waitLatch = CountDownLatch(1)

    fun latchResult(result: Result) {
        this.result = result
        waitLatch.countDown()
    }

    fun waitResult(timeout: Long, unit: TimeUnit): Result? {
        try {
            waitLatch.await(timeout, unit)
        } catch (ignored: InterruptedException) {
            // todo correct handle this
        }
        return result
    }

}