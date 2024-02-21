package com.kagg886.rainbowcourse.dw_dic.runtime.func

import com.kagg886.rainbowcourse.dw_dic.runtime.TestRuntime
import com.kagg886.rainbowcourse.dw_dic.ui.page.impl.MockMessage
import com.kagg886.rainbowcourse.dw_dic.util.Logger
import io.github.seikodictionaryenginev2.base.entity.code.func.Function
import io.github.seikodictionaryenginev2.base.entity.code.func.type.ArgumentLimiter
import io.github.seikodictionaryenginev2.base.session.BasicRuntime

class Log(line: Int, code: String?) : Function(line, code), ArgumentLimiter {
    override fun run(runtime: BasicRuntime<*, *, *>?, args: MutableList<Any>?): Any? {
        Logger.i(args!![0].toString())

        if (runtime is TestRuntime) {
            runtime.list.add(MockMessage(true, "[日志]:${args[0]}"));
        }

        return null
    }

    override fun getArgumentLength(): Int {
        return 1
    }
}