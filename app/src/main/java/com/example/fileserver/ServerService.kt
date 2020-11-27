package com.example.fileserver

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import com.example.fileserver.server.Server

class ServerService : Service() {

    val s=  Server()
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        s.baseFilePath(Environment.getExternalStorageDirectory().absolutePath)
        s.listenAndServe()
        return super.onStartCommand(intent, flags, startId)
    }
}
