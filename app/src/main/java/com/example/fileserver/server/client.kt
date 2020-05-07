package com.example.fileserver.server

import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException
import java.net.Socket
import java.util.*



class clientHandler{

    private lateinit var client: Socket
    private lateinit var baseFilePath : String
    private var starttime = System.currentTimeMillis()
    private var onduty = false

    fun clientHandler(client: Socket,bfilePath : String){
        this.client=client
        this.starttime =  System.currentTimeMillis()
        baseFilePath = bfilePath
        onduty = true
        write(parseReq((read())))
        shutdown()
    }

    private fun write(message: ByteArray) {
        if (client.isConnected){
            client.getOutputStream().write(message)
        }
    }

    private fun read(): ByteArray{
        try {
            if (client.isConnected){
                // we would only care about the first line for now
                //TODO handle the http request in the standard way
                return Scanner(client.getInputStream()).nextLine().toByteArray(Charsets.UTF_8)
            }
        }catch (e: java.lang.InterruptedException){
            return ByteArray(0)
        }
        return ByteArray(0)
    }


    fun onDuty(): Boolean{
        return onduty
    }

    private fun shutdown() {
        onduty= false
        println("${client.inetAddress.hostAddress} closed the connection")
    }

    private fun parseReq(request : ByteArray) : ByteArray{
        val requeststr = request.toString(Charsets.UTF_8).trim()
        val line = requeststr.split("\r|\n")[0].trim()

        val methodresource = line.split(" ")
        val headers = mutableListOf<ByteArray>()

        var resource = (if (baseFilePath!=null ) baseFilePath else "/sdcard") + methodresource[1]
        if (!methodresource[1].trim().equals("/")){
            resource = methodresource[1].trim()
        }

        if (methodresource.count()<3){
            headers.add("HTTP/1.0 400 Not OK".toByteArray(Charsets.UTF_8))
            return response(400,"Invalid Request".toByteArray(Charsets.UTF_8),headers,resource)
        }

        if (!methodresource[0].equals("GET")){
            headers.add("HTTP/1.0 400 Not OK".toByteArray())
            return response(400,"Method not Allowed".toByteArray(Charsets.UTF_8),headers,resource)
        }

        val response = getFIle(resource)

        if (response.size<=0){
            headers.add("HTTP/1.0 400 Not OK".toByteArray(Charsets.UTF_8))
            return response(400,response,headers,resource)
        }
        if (response.toString().equals("File Not Found")){
            headers.add("HTTP/1.0 400 File not found".toByteArray(Charsets.UTF_8))
            return response(404,response,headers,resource)
        }
        headers.add("HTTP/1.0 200 OK".toByteArray(Charsets.UTF_8))
        return response(200,response,headers,resource)
    }

    private fun response(code : Int,body: ByteArray,headers : MutableList<ByteArray>,resource : String): ByteArray{
        headers.add("Server: FileServer/0.1 Kotlin".toByteArray(Charsets.UTF_8)) // lmao? what is this. Did i just invented this
        headers.add(("Date: "+ Calendar.getInstance().time.toString()).toByteArray(Charsets.UTF_8))
        headers.add(("Content-Type: "+getMimeType(resource)).toByteArray(Charsets.UTF_8))
        headers.add(("Content-Length: "+body.size).toByteArray(Charsets.UTF_8))
        headers.add("Cache-Control: no-store".toByteArray(Charsets.UTF_8))

        var finalresponse = headers.reduce { acc, bytes -> acc+bytes+"\n".toByteArray(Charsets.UTF_8) }

        finalresponse=finalresponse.plus("\n".toByteArray())
        finalresponse=finalresponse.plus(body)

        return finalresponse
    }

    private  fun getFIle(filepath : String) : ByteArray{
        try {
            val f = File(filepath)
            Log.e("THERESOURCEC",filepath)
            if (f.isDirectory){
                if (f.listFiles()!=null){
                    Log.e("THERESOURCEC",filepath)
                    return buildHtml(f.listFiles()?.map { it.absolutePath },filepath)
                }

            }
            return f.readBytes()
        }catch (e:IOException){
            Log.e("LETS",e.toString())
            return "File Not Found ".toByteArray()
        }
    }

    fun getMimeType(filepath : String) :String{
        var file : File
        try {
            file = File(filepath)
        }catch (e:IOException){
            return  "text/html;charset=utf-8"
        }
        if (file == null){
            return  "text/html;charset=utf-8"
        }
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString())
        if (ext == null){
            return "text/html;charset=utf-8"
        }
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        return  if ( type ==null)  "text/html;charset=utf-8" else type
    }

    /*
     This Could be better. A templating engine is required
     */
    fun buildHtml(filespath : List<String>?,path: String): ByteArray{
        val startString="""
            <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>$path</title>
</head>
<body>
<h1>Directory listing for $path</h1>
<hr>
<ul>
        """.trim()

        val endString="""
            </ul>
<hr>
</body>
</html>
        """.trim()

        var toAdd = ""
        filespath?.map {
            toAdd= toAdd+"<li><a href=$it>$it</a></li>\n"
        }
        return (startString+toAdd+endString).toByteArray(Charsets.UTF_8)
    }
}