package com.kaspersky.test_server.api

// todo comments
// todo make a wrapper up Result
interface Executor {

    // todo return value?
    fun executeCommand(command: Command): ExecutorResult

}