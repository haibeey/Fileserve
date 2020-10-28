package com.example.fileserver

import android.Manifest
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import  android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat

import androidx.annotation.RequiresApi
import com.google.android.material.button.MaterialButton
import java.net.InetAddress
import java.net.NetworkInterface


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission(READ_EXTERNAL_STORAGE,3434);
        requestPermission(INTERNET,1000)

        val img = findViewById<MaterialButton>(R.id.start_button)
        img.text = "Serving a http  server on ${getDeviceIpAddress()}:6646"
        val scaleDown = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.beacon
        )
        scaleDown.repeatCount = Animation.INFINITE
        scaleDown.duration =5000
        img.startAnimation(scaleDown)

        startService(Intent(this,ServerService::class.java))
    }

    fun getDeviceIpAddress():String{
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        var networkInterface : NetworkInterface
        var inetAddress : InetAddress
        while (networkInterfaces.hasMoreElements()){
            networkInterface = networkInterfaces.nextElement()
            val inetAddresses = networkInterface.inetAddresses
            while (inetAddresses.hasMoreElements()){
                inetAddress = inetAddresses.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress.hostAddress.indexOf(":")<0){
                    if (!inetAddress.hostAddress.startsWith("192"))continue
                    return  inetAddress.hostAddress
                }
            }
        }
        return  "0.0.0.0"
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun  requestPermission(permissionType : String,permissionReqCode: Int): Boolean{

        if (ContextCompat.checkSelfPermission(this,
                permissionType)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permissionType)) {
                //TODO show permission dialogue
            } else {
                // No explanation needed; request the permission
                if (permissionType.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)){
                }else{
                    ActivityCompat.requestPermissions(this,
                        arrayOf(permissionType), permissionReqCode);
                }

            }
            return  false;
        } else {
            // Permission has already been granted
            return  true;
        }
    }
}
