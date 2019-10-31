package com.kaspresky.test_server.log

import com.kaspresky.test_server.log.filter_log.LoggerFilterSystemImpl

/**
 * The singleton to provide Logger interface and to hide an implementation
 */
object LoggerFactory {

    private val loggersMap: MutableMap<String?, Logger> = hashMapOf()
    private var loggerProvider: (tag: String, deviceName: String?) -> Logger =
        { tag, deviceName -> getLoggerForProvider(tag, deviceName) }

    fun getLogger(tag: String, deviceName: String? = null): Logger = loggerProvider.invoke(tag, deviceName)

    private fun getLoggerForProvider(tag: String, deviceName: String?): Logger {
        val key = deviceName
        if (loggersMap.containsKey(key)) {
            return loggersMap[key] ?: throw RuntimeException("It's unbelievable!")
        }
        val logger = LoggerFilterSystemImpl(tag, deviceName)
        loggersMap[key] = logger
        return logger
    }

    /**
     * Only for test purposes
     */
    fun setLogger(loggerProvider: (tag: String, deviceName: String?) -> Logger) {
        this.loggerProvider = loggerProvider
    }
}