package com.kaspersky.test_server.implementation

import com.kaspersky.test_server.api.Command
import com.kaspersky.test_server.api.ConnectionClient
import com.kaspersky.test_server.api.ExecutorResult
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
    private val socket: Socket,
    private val logger: Logger
) : ConnectionClient {

    companion object {
        private val COMMAND_TIMEOUT_MIN = TimeUnit.MINUTES.toSeconds(3)
    }

    private var connectionMaker: ConnectionMaker = ConnectionMaker()
    private val socketMessagesTransferring: SocketMessagesTransferring<ResultMessage<ExecutorResult>, TaskMessage> =
        SocketMessagesTransferring.createTransferring(socket)
    private val commandsInProgress = ConcurrentHashMap<Command, ResultWaiter<ResultMessage<ExecutorResult>>>()

    // todo think about @Synchronized
    @Synchronized
    override fun connect() {
        connectionMaker.connect {
            handleMessages()
        }
    }

    private fun handleMessages() {
        socketMessagesTransferring.startListening(object : MessagesListener<ResultMessage<ExecutorResult>> {
            override fun listenMessages(receiveModel: ResultMessage<ExecutorResult>) {
                // todo log in common and in a case when command is not in the map
                commandsInProgress[receiveModel.command]?.latchResult(receiveModel)
            }
        })
    }

    // todo think about @Synchronized
    @Synchronized
    override fun disconnect() {
        connectionMaker.disconnect {
            socketMessagesTransferring.stopListening()
            socket.close()
            // todo cleaning a line of requests
        }
    }

    override fun executeCommand(command: Command): ExecutorResult {
        val resultWaiter = ResultWaiter<ResultMessage<ExecutorResult>>()
        // todo check a correctness of string value is like a key value in map
        commandsInProgress[command] = resultWaiter
        socketMessagesTransferring.sendMessage(
            TaskMessage(command)
        )
        val resultMessage = resultWaiter.waitResult(COMMAND_TIMEOUT_MIN, TimeUnit.SECONDS)
        commandsInProgress.remove(command)
        // todo output a log of resultMessage
        // todo add description of failed status
        return resultMessage?.data ?: ExecutorResult(ExecutorResultStatus.FAILED, "")
    }

}