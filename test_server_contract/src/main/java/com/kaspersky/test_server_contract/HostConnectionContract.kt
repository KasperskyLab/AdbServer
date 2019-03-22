package com.kaspersky.test_server_contract

import com.kaspersky.test_server_command_handler.Command

const val DEVICE_PORT: Int = 8500

class CmdException(message: String) : RuntimeException(message)
class AdbException(message: String) : RuntimeException(message)

data class CmdCommand(val body: String) : Command<String>()

data class AdbCommand(val body: String) : Command<String>()
