package com.kaspersky.test_server.implementation.transferring

import com.kaspersky.test_server.api.AdbCommand
import java.io.Serializable

internal abstract class Message(open val command: AdbCommand) : Serializable

internal data class TaskMessage(override val command: AdbCommand) : Message(command)

internal data class ResultMessage<T>(override val command: AdbCommand, val data: T) : Message(command)