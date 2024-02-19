package com.kagg886.rainbowcourse.dw_dic.util

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.kagg886.rainbowcourse.dw_dic.App
import io.github.seikodictionaryenginev2.base.entity.DictionaryProject
import io.github.seikodictionaryenginev2.base.env.DICList
import io.github.seikodictionaryenginev2.base.env.DictionaryEnvironment
import io.github.seikodictionaryenginev2.base.model.DictionarySetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.lang.reflect.Modifier
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

fun DICList.findProjectByName(name: String): DictionaryProject {
    return DICList.INSTANCE.filter {
        name == it.name
    }.toList()[0]
}

fun DictionaryEnvironment.getSettingByName(name: String): DictionarySetting {
    return DictionaryEnvironment.getInstance().getSetting(DICList.INSTANCE.findProjectByName(name))
}

fun openURL(url: String) {
    App.getApp().startActivity(Intent().apply {
        this.action = Intent.ACTION_VIEW
        this.flags = FLAG_ACTIVITY_NEW_TASK
        data = Uri.parse(url)
    })
}

fun File.deleteDir(): Boolean {
    if (!isDirectory) {
        delete()
        return true
    }
    if (exists()) {
        val files = listFiles()
        if (files != null) { // 若directory是个文件夹
            for (file in files) {
                if (file.isDirectory) {
                    file.deleteDir()
                } else {
                    file.delete()
                }
            }
        }
    }
    // 文件夹现在为空，可以删除
    return delete()
}

@Composable
fun <T> Flow<T>.collectAsRememberedMutableState(initial: T): MutableState<T> {
    val state = collectAsState(initial = initial)
    return remember {
        mutableStateOf(state.value)
    }
}