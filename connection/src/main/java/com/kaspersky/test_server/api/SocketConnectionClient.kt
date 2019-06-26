package com.kaspersky.test_server.api

interface SocketConnectionClient<Result> : BaseSocketConnection, Executor<Result>