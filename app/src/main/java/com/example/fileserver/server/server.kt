package com.example.fileserver.server


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket


class Server {
    var isAccepting = true
    var port: Int = 6646

    val server: ServerSocket = ServerSocket(port)
    lateinit var basefilepath : String

    fun baseFilePath(filepath :String){
        basefilepath = filepath
    }

    fun listenAndServe(){
        GlobalScope.launch {
            while (isAccepting){
                val c = server.accept()
                async { handleRequest(c) }
            }
        }

    }

    fun handleRequest(c: Socket){
        clientHandler().clientHandler(c,basefilepath)
    }

    fun close(){
        isAccepting=false
        try {
            server.close()
        }catch (e: Exception){

        }
    }
}