package com.example.fileserver

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.example.fileserver.server.Server
import java.net.URL

class ServerService : Service() {

    val s=  Server()
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        s.baseFilePath(Environment.getExternalStorageDirectory().absolutePath)
        s.listenAndServe()
    }

}
