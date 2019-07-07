package com.kaspersky.test_server

import java.lang.RuntimeException

internal class ConnectionTimeException(override val message: String) : RuntimeException(message)