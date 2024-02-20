package com.kagg886.rainbowcourse.dw_dic.runtime

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.kagg886.rainbowcourse.dw_dic.ui.page.impl.MockMessage
import com.kagg886.rainbowcourse.dw_dic.util.Logger
import io.github.seikodictionaryenginev2.base.entity.DictionaryFile
import io.github.seikodictionaryenginev2.base.session.BasicRuntime

class TestRuntime(file: DictionaryFile?, event: String?, private val l: SnapshotStateList<MockMessage>): BasicRuntime<String,String,StringBuilder>(file, event) {
    override fun initContact(EVENT: String?): String = "";

    override fun initMessageCache(): StringBuilder = StringBuilder()

    override fun initObject(command: String?, event: String?) {
        super.initObject(command, event)
        runtimeObject["上下文"] = command
    }

    override fun appendMessage(str: String?) {
        messageCache.append(str)
    }

    override fun clearMessage0(cache: StringBuilder?) {
        l.add(MockMessage(true,cache.toString()))
        Logger.i(cache.toString())
    }
}