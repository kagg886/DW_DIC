package com.kagg886.rainbowcourse.dw_dic

import com.kagg886.rainbowcourse.dw_dic.util.Promise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun testIR() {
        val ir = Promise<String, String> {
            Thread {
                Thread.sleep(2000)
                resolve("awa")
            }.start()
        }

        runBlocking {
            withContext(Dispatchers.IO) {
                val s = ir.startForResult("qwq")
                println(s)
            }
        }
    }

    @Test
    fun testFlow() = runBlocking {
        val flow = flow<Int> {
            emit(1)
            delay(1000)
            emit(2)
            delay(1000)
            emit(3)
        }.collect {
            println(it)
        }
        println("aaa")
    }
}