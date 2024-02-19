package com.kagg886.rainbowcourse.dw_dic

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import ayzf.mcsq.dw.msg.Messenger
import com.kagg886.rainbowcourse.dw_dic.util.Logger
import io.github.seikodictionaryenginev2.base.command.Registrator
import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCommandMatcher
import io.github.seikodictionaryenginev2.base.env.DICList
import io.github.seikodictionaryenginev2.base.env.DictionaryEnvironment
import kotlinx.coroutines.flow.map
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.nio.file.Paths


class App : Application() {
    private val setting: DataStore<Preferences> by preferencesDataStore(name = "setting")

    var pluginService: PluginService? = null
        set(value) {
            field = value
            Logger.i("PluginService装载成功$value")
        }

    @Composable
    fun <T> rememberPreferenceState(
        key: Preferences.Key<T>,
        default: T
    ): MutableState<T> {
        val valueState = remember { mutableStateOf(default) }

        LaunchedEffect(key1 = setting) {
            setting.data.map { preferences ->
                preferences[key] ?: default
            }.collect { valueState.value = it }
        }

        LaunchedEffect(key1 = valueState.value) {
            // Save the value to DataStore when the MutableState's value changes
            setting.edit { preferences ->
                preferences[key] = valueState.value
            }
        }

        return valueState
    }

    override fun onCreate() {
        super.onCreate()
        val root = getExternalFilesDir("files")?.parentFile?.absolutePath
        DictionaryEnvironment.getInstance().setDicConfigPoint(
            Paths.get(root, "config.json").toFile().absolutePath
        )
        DictionaryEnvironment.getInstance().dicRoot = Paths.get(root, "dic").toFile().apply {
            this.mkdirs()
        }

        DictionaryEnvironment.getInstance().dicData = Paths.get(root, "data").toAbsolutePath().apply {
            this.toFile().mkdirs()
        }

        DICList.INSTANCE.refresh()
        Registrator.inject()

        DictionaryCommandMatcher.domainQuoteNew["群"] = arrayOf(Messenger::class.java)
        DictionaryCommandMatcher.domainQuoteNew["测试"] = arrayOf(String::class.java)
        Logger.i("词库引擎注入完毕")
    }

    companion object {
        @Suppress("all")
        fun getApp(): App {
            var application: Application? = null
            try {
                val atClass = Class.forName("android.app.ActivityThread")
                val currentApplicationMethod = atClass.getDeclaredMethod("currentApplication")
                currentApplicationMethod.isAccessible = true
                application = currentApplicationMethod.invoke(null) as Application
            } catch (ignored: Exception) {
            }
            if (application != null) return application as App
            try {
                val atClass = Class.forName("android.app.AppGlobals")
                val currentApplicationMethod = atClass.getDeclaredMethod("getInitialApplication")
                currentApplicationMethod.isAccessible = true
                application = currentApplicationMethod.invoke(null) as Application
            } catch (ignored: Exception) {
            }
            return application as App
        }

        @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
        fun currentActivity(): ComponentActivity {
            var current: ComponentActivity? = null
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null
                )
                val activitiesField: Field = activityThreadClass.getDeclaredField("mActivities")
                activitiesField.isAccessible = true
                for (activityRecord in (activitiesField.get(activityThread) as Map<*, *>).values) {
                    val activityRecordClass: Class<*> = activityRecord!!.javaClass
                    val pausedField: Field = activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    if (!pausedField.getBoolean(activityRecord)) {
                        val activityField: Field = activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        current = activityField.get(activityRecord) as ComponentActivity?
                    }
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            return current ?: throw IllegalStateException("Background!")
        }
    }
}