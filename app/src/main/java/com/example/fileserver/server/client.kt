package com.example.fileserver.server

import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.Socket
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.Charset
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
        val responses = parseReq((read()))

        try {
            write(responses.first,responses.second)
            shutdown()
        }catch (E :Exception){

        }

    }

    private fun write(header: InputStream,body :InputStream) {
        if (client.isConnected){
            val sendArr =  ByteBuffer.allocate(10000000).array()
            var stream = header
            var sendSize = stream.read(sendArr)
            var change = 0
            val outStream = client.getOutputStream()
            while (sendSize>0){
                outStream.write(
                    sendArr.sliceArray(0 until sendSize)
                )
                sendSize = stream.read(sendArr)
                if (sendSize<=0){
                    if (change<=0){
                        change =1
                        stream = body
                        sendSize = stream.read(sendArr)
                    }
                }
            }
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
        Thread.sleep(100)
        client.close()
    }


    private fun parseReq(request : ByteArray) : Pair<InputStream,InputStream>{

        val requeststr = request.toString(Charsets.UTF_8).trim()
        val line = requeststr.split("\r|\n")[0].trim()

        val methodresource = line.split(" ")
        val headers = mutableListOf<ByteArray>()

        var resource = (if (baseFilePath!=null ) baseFilePath else "/sdcard") + methodresource[1]
        if (methodresource[1].trim() != "/"){
            resource = methodresource[1].trim()
        }
        resource  = URLDecoder.decode(resource,"UTF-8")
        if (methodresource.count()<3){
            headers.add("HTTP/1.0 400 Not OK".toByteArray(Charsets.UTF_8))
            return Pair(
                response(headers,resource).inputStream(),
                "Invalid Request".toByteArray(Charsets.UTF_8).inputStream()
            )
        }

        if (!methodresource[0].equals("GET")){
            headers.add("HTTP/1.0 400 Not OK".toByteArray())
            return Pair(
                response(headers,resource).inputStream(),
                "Method not Allowed".toByteArray(Charsets.UTF_8).inputStream()
            )
        }

        val response = getFIle(resource)

        if (response.toString().equals("File Not Found")){
            headers.add("HTTP/1.0 400 File not found".toByteArray(Charsets.UTF_8))
            return Pair(
                response(headers,resource).inputStream(),
                "".toByteArray(Charsets.UTF_8).inputStream()
            )
        }
        headers.add("HTTP/1.0 200 OK".toByteArray(Charsets.UTF_8))
        headers.add("Connection: Keep-Alive".toByteArray(Charsets.UTF_8))
        if (isFile(resource)){
            headers.add(("Content-Length: "+getFileSize(resource)).toByteArray(Charsets.UTF_8))
        }
        return Pair(response(headers,resource).inputStream(),response)
    }

    private fun response(headers : MutableList<ByteArray>,resource : String): ByteArray{
        headers.add("Server: FileServer/0.1 Kotlin".toByteArray(Charsets.UTF_8)) // lmao? what is this. Did i just invented this
        headers.add(("Date: "+ Calendar.getInstance().time.toString()).toByteArray(Charsets.UTF_8))
        headers.add(("Content-Type: "+getMimeType(resource)).toByteArray(Charsets.UTF_8))
        headers.add("Cache-Control: no-store".toByteArray(Charsets.UTF_8))

        var finalresponse = headers.reduce { acc, bytes -> acc+bytes+"\n".toByteArray(Charsets.UTF_8) }

        finalresponse=finalresponse.plus("\n".toByteArray())

        return finalresponse
    }

    private  fun getFIle(filepath : String) : InputStream{
        try {
            val f = File(filepath)
            if (f.isDirectory){
                if (f.listFiles()!=null){
                    return buildHtml(f.listFiles()?.map { it.absolutePath },filepath).inputStream()
                }
            }
            return f.inputStream()
        }catch (e:IOException){
            return "File Not Found ".toByteArray().inputStream()
        }
    }

    private fun isFile(filepath : String): Boolean{
        return try {
            File(filepath).isFile
        }catch (e:IOException){
            false
        }
    }

    private fun getFileSize(filepath: String):Long{
        return try {
            val f = File(filepath)
            f.length()
        }catch (e:IOException){
            0
        }
    }

    private fun getMimeType(filepath : String) :String{
        val file : File
        try {
            file = File(filepath)
        }catch (e:IOException){
            return  "text/html;charset=utf-8"
        }
        if (file == null){
            return  "text/html;charset=utf-8"
        }
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString())
            ?: return "text/html;charset=utf-8"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/html;charset=utf-8"
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
            toAdd= "$toAdd<li><a href='$it'>$it</a></li>\n"
        }
        return (startString+toAdd+endString).toByteArray(Charsets.UTF_8)
    }
}