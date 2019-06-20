package com.kaspersky.test_server_command_handler.local

import com.kaspersky.test_server_command_handler.Command
import com.kaspersky.test_server_command_handler.ICommandExecutor
import com.kaspersky.test_server_command_handler.ICommandHandler
import java.util.*

class LocalCommandExecutor(vararg handlers: ICommandHandler) : ICommandExecutor {

    private val mHandlers = ArrayList<ICommandHandler>()

    init {
        mHandlers.addAll(handlers)
    }


    @Throws(Exception::class)
    override fun <T> execute(command: Command<T>): T {
        for (handler in mHandlers) {
            if (handler.accept(command)) {
                return handler.execute(command)
            }
        }
        throw RuntimeException("Unknown command : $command")
    }

    fun addHandler(handler: ICommandHandler): LocalCommandExecutor {
        mHandlers.add(handler)
        return this
    }
}
