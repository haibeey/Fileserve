package com.example.fileserver

import android.content.Context
import com.example.fileserver.server.Server
import org.junit.Test

import org.junit.Assert.*
import java.net.HttpURLConnection
import java.net.URL


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_server(){
        val s=  Server()
        Thread{
            s.listenAndServe()
        }

        println(URL("http://localhost:6646").readText())
        s.close()
    }
}
