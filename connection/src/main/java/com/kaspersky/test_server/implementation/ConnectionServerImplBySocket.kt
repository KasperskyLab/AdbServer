package com.kaspersky.test_server.implementation

import com.kaspersky.test_server.api.CommandExecutor
import com.kaspersky.test_server.api.ConnectionServer
import com.kaspersky.test_server.api.CommandResult
import com.kaspersky.test_server.implementation.light_socket.LightSocketWrapperImpl
import com.kaspersky.test_server.implementation.transferring.ResultMessage
import com.kaspersky.test_server.implementation.transferring.SocketMessagesTransferring
import com.kaspersky.test_server.implementation.transferring.TaskMessage
import com.kaspresky.test_server.log.Logger
import java.net.Socket
import java.util.concurrent.Executors

internal class ConnectionServerImplBySocket(
    private val socketCreation: () -> Socket,
    private val adbCommandExecutor: CommandExecutor,
    private val logger: Logger
) : ConnectionServer {

    private val tag = javaClass.simpleName

    private var connectionMaker: ConnectionMaker = ConnectionMaker(logger)
    private lateinit var socketMessagesTransferring: SocketMessagesTransferring<TaskMessage, ResultMessage<CommandResult>>

    private var _socket: Socket? = null
    private val socket: Socket
        get() =_socket ?: throw IllegalStateException("tryConnect must be called first")

    // todo replace newCachedThreadPool
    private val backgroundExecutor = Executors.newCachedThreadPool()

    override fun tryConnect() {
        logger.i(tag, "tryConnect", "start")
        connectionMaker.connect(
            connectAction = { _socket = socketCreation.invoke() },
            successConnectAction = {
                logger.i(tag, "tryConnect", "start handleMessages")
                handleMessages()
            }
        )
        logger.i(tag, "tryConnect", "attempt completed")
    }

    private fun handleMessages() {
        socketMessagesTransferring = SocketMessagesTransferring.createTransferring(
            lightSocketWrapper = LightSocketWrapperImpl(socket),
            logger = logger,
            disruptAction = { tryDisconnect() }
        )
        socketMessagesTransferring.startListening { taskMessage ->
            logger.i(tag, "handleMessages", "received taskMessage=$taskMessage")
            backgroundExecutor.execute {
                val result = adbCommandExecutor.execute(taskMessage.command)
                logger.i(tag, "handleMessages.backgroundExecutor", "result of taskMessage=$taskMessage => result=$result")
                socketMessagesTransferring.sendMessage(
                    ResultMessage(taskMessage.command, result)
                )
            }
        }
    }
    
    override fun tryDisconnect() {
        logger.i(tag, "tryDisconnect", "start")
        connectionMaker.disconnect {
            socketMessagesTransferring.stopListening()
            socket.close()
        }
        logger.i(tag, "tryDisconnect", "attempt completed")
    }

    override fun isConnected(): Boolean =
        connectionMaker.isConnected()

}