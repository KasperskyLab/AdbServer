package com.kaspresky.test_server.log

/**
 * The singleton to provide Logger interface and to hide an implementation
 */
object LoggerFactory {

    private var loggerProvider: (tag: String, deviceName: String?) -> Logger =
        { tag, deviceName -> LoggerSystemImpl(tag, deviceName) }

    fun getLogger(tag: String, deviceName: String? = null): Logger = LoggerSystemImpl(tag, deviceName)

    /**
     * Only for test purposes
     */
    fun setLogger(loggerProvider: (tag: String, deviceName: String?) -> Logger) {
        this.loggerProvider = loggerProvider
    }
}