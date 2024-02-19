package com.kagg886.rainbowcourse.dw_dic.ui.page.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kagg886.rainbowcourse.dw_dic.App
import com.kagg886.rainbowcourse.dw_dic.MainActivity
import com.kagg886.rainbowcourse.dw_dic.util.DialogSetting
import com.kagg886.rainbowcourse.dw_dic.util.collectAsRememberedMutableState
import com.kagg886.rainbowcourse.dw_dic.util.openURL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class SettingItem(
    val key: String,
    val title: String,
    val subtitle: String? = "",
    var type: SettingType = SettingType.NONE,
    val onClick: () -> Unit = {}
)

enum class SettingType {
    BOOL, INPUT, CHECKBOX, NONE
}

@Preview
@Composable
fun SettingPage() {

    val settingsItems = listOf(
        SettingItem(
            key = "refreshAuto",
            title = "有新消息自动刷新词库",
            subtitle = "重启程序生效",
            type = SettingType.BOOL
        ),
//        SettingItem(title = "Bluetooth", subtitle = "Connected devices", type = SettingType.INPUT),
        SettingItem(
            key = "ver",
            title = "软件版本",
            subtitle = App.getApp().packageManager.getPackageInfo(App.getApp().packageName, 0).versionName
        ),
        SettingItem(
            key = "author",
            title = "作者",
            subtitle = "kagg886"
        ),
        SettingItem(
            key = "source",
            title = "程序源码",
            subtitle = "https://github.com/kagg886/DW_DIC",
            onClick = {
                openURL("https://github.com/kagg886/DW_DIC")
            }
        ),
        // 更多设置项...
    )

    LazyColumn {
        items(settingsItems) {
            SettingRow(settingItem = it)
            Divider()
        }
    }
}

@Composable
fun SettingRow(settingItem: SettingItem) {

    var value by App.getApp().rememberPreferenceState(stringPreferencesKey(settingItem.key),"")

    ListItem(
        headlineContent = { Text(settingItem.title) },
        modifier = Modifier.padding(vertical = 8.dp).clickable {
            if (settingItem.type == SettingType.INPUT) {
                CoroutineScope(Dispatchers.Main).launch {
                    val result = (App.currentActivity() as MainActivity).dialogOpener.startForResult(
                        DialogSetting(settingItem.title, "请输入", value)
                    )
                    value = result
                }
            }
            settingItem.onClick()
        },
        overlineContent = null,
        supportingContent = settingItem.subtitle?.let { { Text(it) } },
        leadingContent = null,
        trailingContent = {
            when (settingItem.type) {
                SettingType.BOOL -> {
                    Switch(
                        checked = value.toBoolean(),
                        onCheckedChange = {
                            value = it.toString()
                        }
                    )
                }

                else -> {}
            }
        },
        colors = ListItemDefaults.colors(),
        tonalElevation = ListItemDefaults.Elevation,
        shadowElevation = ListItemDefaults.Elevation
    )
}