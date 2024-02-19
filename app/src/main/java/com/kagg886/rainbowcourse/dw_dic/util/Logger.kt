package com.kagg886.rainbowcourse.dw_dic.util;

import android.util.Log;

interface Logger {
    fun i(msg: String)
    fun w(msg: String, e: Throwable? = null)
    fun e(msg: String, e: Throwable? = null)

    companion object : Logger {
        private val receiver = mutableListOf(
                AndroidLogger
        )

        override fun i(msg: String) {
            receiver.forEach {
                AndroidLogger.i(msg)
            }
        }

        override fun w(msg: String, e: Throwable?) {
            receiver.forEach {
                AndroidLogger.w(msg, e)
            }
        }

        override fun e(msg: String, e: Throwable?) {
            receiver.forEach {
                AndroidLogger.e(msg, e)
            }
        }
    }
}



object AndroidLogger : Logger {

    private fun getTag(): String {
        val t = Throwable().stackTrace[3]
        return "${t.fileName}: ${t.lineNumber}";
    }

    override fun i(msg: String) {
        Log.i("DW_DIC_LOG", "[${getTag()}]: $msg")
    }

    override fun w(msg: String, e: Throwable?) {
        Log.w("DW_DIC_LOG", "[${getTag()}]: $msg", e)
    }

    override fun e(msg: String, e: Throwable?) {
        Log.e("DW_DIC_LOG", "[${getTag()}]: $msg", e)
    }

}