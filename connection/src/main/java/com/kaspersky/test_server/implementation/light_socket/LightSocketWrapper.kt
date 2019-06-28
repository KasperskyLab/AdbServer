package com.kaspersky.test_server.implementation.light_socket

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// todo comments
internal interface LightSocketWrapper {

    @Throws(IOException::class)
    fun getOutputStream(): OutputStream

    @Throws(IOException::class)
    fun getInputStream(): InputStream

}