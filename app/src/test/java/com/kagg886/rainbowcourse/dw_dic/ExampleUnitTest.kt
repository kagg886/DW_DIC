package com.kagg886.rainbowcourse.dw_dic

import com.kagg886.rainbowcourse.dw_dic.util.Promise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private lateinit var ir: Promise<String, String>

    @Test
    fun testIR() {
        ir = Promise {
            Thread {
                Thread.sleep(2000)
                ir.resolve("awa")
            }.start()
        }

        runBlocking {
            withContext(Dispatchers.IO) {
                val s = ir.startForResult("qwq")
                println(s)
            }
        }
    }
}