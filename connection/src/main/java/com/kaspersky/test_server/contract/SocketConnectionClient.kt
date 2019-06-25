package com.kaspersky.test_server.contract

interface SocketConnectionClient<Result> : BaseSocketConnection, Executor<Result>