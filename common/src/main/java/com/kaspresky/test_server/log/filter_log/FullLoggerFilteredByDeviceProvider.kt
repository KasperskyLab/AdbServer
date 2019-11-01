package com.kaspresky.test_server.log.filter_log

import com.kaspresky.test_server.log.full_logger.FullLogger
import com.kaspresky.test_server.log.full_logger.FullLoggerSystemImpl

internal class FullLoggerFilteredByDeviceProvider : FullLogger {

    private val loggersMap: MutableMap<String?, FullLogger> = hashMapOf()

    override fun log(
        logType: FullLogger.LogType?,
        deviceName: String?,
        tag: String?,
        method: String?,
        text: String?
    ) {
        getFullLogger(deviceName).log(logType, deviceName, tag, method, text)
    }

    private fun getFullLogger(deviceName: String?): FullLogger {
        if (loggersMap.containsKey(deviceName)) {
            return loggersMap[deviceName] ?: throw RuntimeException("It's unbelievable!")
        }
        val fullLogger = LoggerAnalyzer(
            FullLoggerSystemImpl()
        )
        loggersMap[deviceName] = fullLogger
        return fullLogger
    }
}