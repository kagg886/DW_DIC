package com.kagg886.rainbowcourse.dw_dic

import android.content.Intent
import android.os.IBinder
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.stringPreferencesKey
import ayzf.mcsq.dw.msg.Messenger
import ayzf.mcsq.dw.msg.Msg
import ayzf.mcsq.dw.plugin.PluginBinder
import ayzf.mcsq.dw.plugin.PluginBinderHandler
import com.kagg886.rainbowcourse.dw_dic.runtime.WeChatRuntime
import com.kagg886.rainbowcourse.dw_dic.runtime.get
import com.kagg886.rainbowcourse.dw_dic.util.Logger
import io.github.seikodictionaryenginev2.base.env.DICList
import io.github.seikodictionaryenginev2.base.env.DictionaryEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PluginService: PluginBinder(), PluginBinderHandler {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        Logger.i("插件开始回绑")
        return super.newBinder(intent, this)
    }

    override fun onLoad() {
        App.getApp().pluginService = this
    }

    override fun onUnLoad() {
    }

    override fun onMsgHandler(p0: Messenger) {

        scope.launch {
            val a = App.getApp().setting.data.map {
                it[stringPreferencesKey("refreshAuto")] ?: "false"
            }.first().toBoolean()
            if (a) {
                Logger.w("警告:词库自动刷新已开启")
                DICList.INSTANCE.refresh()
            }

            DICList.INSTANCE.forEach {
                if (!it.isInited) {
                    return@forEach
                }
                if (!DictionaryEnvironment.getInstance().getSetting(it).isEnabled) {
                    return@forEach
                }

                val runtime = WeChatRuntime(it.indexFile, p0)
                try {
                    runtime.invoke(p0[Msg.SYSTEM_MSGTEXT])
                } catch (e: Exception) {
                    Logger.e(e.message!!, e)
                }
            }
        }
        //        String msg = messenger.getString();
        //        if (msg.equals("测试"))
        //        {
        //            super.printI("测试日志");
        //            super.send(new Messenger.Builder() {
        //                @Override
        //                public void build(Messenger msg)
        //                {
        //                    msg.addMsg(Msg.SYSTEM_SENDID, messenger.getString(Msg.SYSTEM_SENDID));
        //                    msg.addMsg(Msg.SYSTEM_MSGTYPE, Msg.MSGTYPE_TEXT);
        //                    msg.addMsg(Msg.SYSTEM_MSGTEXT, "测试成功");
        //                }
        //            });
        //        }
        //    }
    }

    private val icon by lazy {
        resources.getDrawable(R.drawable.ic_launcher, theme)!!.toBitmap(250, 250)
    }

    override fun icon() = icon

    override fun name() = "DW_DIC"

    override fun info() = "SeikoDIC_适用于DW平台"

    override fun author(): String = "kagg886"

    override fun version(): String = packageManager.getPackageInfo(packageName,0).versionName

    override fun activity(): String = MainActivity::class.java.name
}