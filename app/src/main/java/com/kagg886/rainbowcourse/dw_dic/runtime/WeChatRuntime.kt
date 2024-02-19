package com.kagg886.rainbowcourse.dw_dic.runtime

import ayzf.mcsq.dw.msg.Messenger
import ayzf.mcsq.dw.msg.Msg
import com.kagg886.rainbowcourse.dw_dic.App
import com.kagg886.rainbowcourse.dw_dic.PluginService
import io.github.seikodictionaryenginev2.base.entity.DictionaryFile
import io.github.seikodictionaryenginev2.base.exception.DictionaryOnRunningException
import io.github.seikodictionaryenginev2.base.session.BasicRuntime

class WeChatRuntime(file: DictionaryFile, event: Messenger) : BasicRuntime<Messenger, String, Messenger>(file, event) {

    override fun initContact(eve: Messenger): String = eve[Msg.SYSTEM_SENDID]

    override fun initMessageCache(): Messenger {
        return Messenger.newObject()
    }

    override fun initObject(command: String?, event: Messenger?) {
        super.initObject(command, event)

        runtimeObject["上下文"] = command
    }

    override fun appendMessage(str: String) {
        messageCache[Msg.SYSTEM_MSGTEXT] = str
    }

    override fun clearMessage0(cache: Messenger) {
        try {
            App.getApp().pluginService!!.send(cache)
        } catch (e:Exception) {
            throw DictionaryOnRunningException("插件掉线")
        }
    }
}

operator fun Messenger.set(tag: String, value: Any) {
    this.addMsg(tag, value)
}

operator fun Messenger.get(key: String): String {
    return this.getString(key);
}