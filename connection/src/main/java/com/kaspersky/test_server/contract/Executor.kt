package com.kaspersky.test_server.contract

// todo comments
// todo make a wrapper up Result
interface Executor<Result> {

    // todo return value?
    fun executeCommand(command: Command): Result

}