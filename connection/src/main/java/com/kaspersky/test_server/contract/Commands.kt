package com.kaspersky.test_server.contract

import java.io.Serializable

// todo think about Generics
sealed class Command<T> : Serializable

data class AdbCommand(val body: String) : Command<String>()
data class CmdCommand(val body: String) : Command<String>()