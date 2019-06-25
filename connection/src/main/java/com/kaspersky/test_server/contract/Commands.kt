package com.kaspersky.test_server.contract

import java.io.Serializable

sealed class Command : Serializable

data class AdbCommand(val body: String) : Command()
data class CmdCommand(val body: String) : Command()