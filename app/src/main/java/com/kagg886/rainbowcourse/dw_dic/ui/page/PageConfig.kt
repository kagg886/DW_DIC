package com.kagg886.rainbowcourse.dw_dic.ui.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.kagg886.rainbowcourse.dw_dic.ui.page.impl.HomePage
import com.kagg886.rainbowcourse.dw_dic.ui.page.impl.MockPage
import com.kagg886.rainbowcourse.dw_dic.ui.page.impl.SettingPage

object PageConfig {
    val list = listOf(
        PageItem("首页", Icons.Outlined.Home, "MainPage") @Composable { HomePage() },
        PageItem("测试", Icons.Outlined.Done, "MockPage") @Composable { MockPage() },
        PageItem("设置", Icons.Outlined.Settings, "SettingPage") @Composable { SettingPage() },
    )

    const val DEFAULT_ROUTER = "MainPage"

    fun findPageByRouter(string: String): PageItem {
        return list.filter {
            it.router == string
        }.toList()[0]
    }
}

data class PageItem(val title: String, val icon: ImageVector, val router: String, val widget: @Composable () -> Unit)