package com.kaspersky.test_server.api

import java.io.Serializable

abstract class Command(open val body: String) : Serializable