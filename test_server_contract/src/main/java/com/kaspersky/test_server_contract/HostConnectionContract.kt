package com.kaspersky.test_server_contract

import com.kaspersky.test_server_command_handler.Command

const val PORT: Int = 8500

class CmdException(message: String) : RuntimeException(message)

data class CmdCommand(val body: String) : Command<String>()
