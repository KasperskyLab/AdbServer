package com.kaspersky.test_server.implementation.transferring

internal interface MessagesListener<ReceiveModel> {

    fun listenMessages(receiveModel: ReceiveModel)

}