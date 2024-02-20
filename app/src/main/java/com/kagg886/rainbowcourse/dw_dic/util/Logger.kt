package com.kagg886.rainbowcourse.dw_dic.util;

import android.util.Log;
import com.kagg886.rainbowcourse.dw_dic.App
import io.github.seikodictionaryenginev2.base.util.IOUtil

interface Logger {
    fun i(msg: String)
    fun w(msg: String, e: Throwable? = null)
    fun e(msg: String, e: Throwable? = null)

    companion object : Logger {
        private val receiver = mutableListOf(
            AndroidLogger,
            BridgeLogger
        )

        override fun i(msg: String) {
            receiver.forEach {
                it.i(msg)
            }
        }

        override fun w(msg: String, e: Throwable?) {
            receiver.forEach {
                it.w(msg, e)
            }
        }

        override fun e(msg: String, e: Throwable?) {
            receiver.forEach {
                it.e(msg, e)
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

object BridgeLogger : Logger {
    override fun i(msg: String) {
        App.getApp().pluginService?.printI(msg)
    }

    override fun w(msg: String, e: Throwable?) {
        val info = if (e == null) "" else IOUtil.getException(e)
        App.getApp().pluginService?.printW("${msg}\n${info}")
    }

    override fun e(msg: String, e: Throwable?) {
        val info = if (e == null) "" else IOUtil.getException(e)
        App.getApp().pluginService?.printE("${msg}\n${info}")
    }

}