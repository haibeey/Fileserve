package com.example.fileserver.server


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket


class Server {
    var isAccepting = true
    var port: Int = 6646

    private val server: ServerSocket = ServerSocket(port)
    lateinit var basefilepath : String

    fun baseFilePath(filepath :String){
        basefilepath = filepath
    }

    fun listenAndServe(){
        try {
            Thread{
                while (isAccepting){
                    val c = server.accept()
                    c.keepAlive = true
                    Thread{
                        handleRequest(c)
                    }.start()
                }
            }.start()

            GlobalScope.launch {
                val ticker = ticker(delayMillis = 1000, initialDelayMillis = 0)
                for (event in ticker){

                }
            }
        }catch (e : Exception){}


    }

    private fun handleRequest(c: Socket){
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