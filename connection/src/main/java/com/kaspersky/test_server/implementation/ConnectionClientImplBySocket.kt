package com.kaspersky.test_server.implementation

import com.kaspersky.test_server.api.AdbCommand
import com.kaspersky.test_server.api.ConnectionClient
import com.kaspersky.test_server.api.CommandResult
import com.kaspersky.test_server.api.ExecutorResultStatus
import com.kaspersky.test_server.implementation.transferring.MessagesListener
import com.kaspersky.test_server.implementation.transferring.ResultMessage
import com.kaspersky.test_server.implementation.transferring.SocketMessagesTransferring
import com.kaspersky.test_server.implementation.transferring.TaskMessage
import com.kaspresky.test_server.log.Logger
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

internal class ConnectionClientImplBySocket(
    private val socketCreation: () -> Socket,
    private val logger: Logger
) : ConnectionClient {

    companion object {
        private val COMMAND_TIMEOUT_MIN = TimeUnit.MINUTES.toSeconds(3)
    }

    private lateinit var socket: Socket
    private var connectionMaker: ConnectionMaker = ConnectionMaker(logger)
    private lateinit var socketMessagesTransferring: SocketMessagesTransferring<ResultMessage<CommandResult>, TaskMessage>
    private val commandsInProgress = ConcurrentHashMap<AdbCommand, ResultWaiter<ResultMessage<CommandResult>>>()

    // todo think about @Synchronized
    @Synchronized
    override fun connect() {
        logger.i(javaClass.simpleName, "connect() start")
        connectionMaker.connect {
            socket = socketCreation.invoke()
            handleMessages()
        }
        logger.i(javaClass.simpleName, "connect() completed")
    }

    private fun handleMessages() {
        socketMessagesTransferring = SocketMessagesTransferring.createTransferring(socket, logger)
        socketMessagesTransferring.startListening(object : MessagesListener<ResultMessage<CommandResult>> {
            override fun listenMessages(receiveModel: ResultMessage<CommandResult>) {
                // todo log in common and in a case when command is not in the map
                logger.i(javaClass.simpleName, "handleMessages() received message=$receiveModel")
                commandsInProgress[receiveModel.command]?.latchResult(receiveModel)
            }
        })
    }

    // todo think about @Synchronized
    @Synchronized
    override fun disconnect() {
        logger.i(javaClass.simpleName, "disconnect() start")
        connectionMaker.disconnect {
            socket.close()
            // todo cleaning a line of requests
        }
        logger.i(javaClass.simpleName, "disconnect() completed")
    }

    override fun isConnected(): Boolean =
        connectionMaker.isConnected()

    override fun executeAdbCommand(command: AdbCommand): CommandResult {
        logger.i(javaClass.simpleName, "executeAdbCommand(command=$command) started")
        val resultWaiter = ResultWaiter<ResultMessage<CommandResult>>()
        // todo check a correctness of string value is like a key value in map
        commandsInProgress[command] = resultWaiter
        socketMessagesTransferring.sendMessage(
            TaskMessage(command)
        )
        val resultMessage = resultWaiter.waitResult(COMMAND_TIMEOUT_MIN, TimeUnit.SECONDS)
        commandsInProgress.remove(command)
        // todo output a log of resultMessage
        // todo add description of failed status
        val commandResult = resultMessage?.data ?: CommandResult(ExecutorResultStatus.FAILED, "")
        logger.i(javaClass.simpleName, "executeAdbCommand(command=$command) completed with commandResult=$commandResult")
        return commandResult
    }

}