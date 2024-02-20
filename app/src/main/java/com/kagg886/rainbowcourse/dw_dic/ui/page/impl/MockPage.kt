package com.kagg886.rainbowcourse.dw_dic.ui.page.impl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kagg886.rainbowcourse.dw_dic.App
import com.kagg886.rainbowcourse.dw_dic.runtime.TestRuntime
import io.github.seikodictionaryenginev2.base.env.DICList
import io.github.seikodictionaryenginev2.base.util.IOUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class MockMessage(val isBot: Boolean, val msg: String)

@Composable
fun MockPage() {
    val messageList = remember {
        mutableStateListOf(MockMessage(true, "请发送消息!"))
    }

    var refresh by remember {
        mutableStateOf(true)
    }
    val state = rememberSwipeRefreshState(refresh)

    LaunchedEffect(refresh) {
        messageList.clear()
        messageList.add(MockMessage(true, "请发送消息!"))
        refresh = false
    }

    var msg by remember {
        mutableStateOf("测试")
    }

    val listState = rememberLazyListState()
    val scrollState = rememberScrollState()
    LaunchedEffect(messageList.size) {
        listState.animateScrollToItem(index = messageList.size - 1)
    }

    var dialog by App.getApp().rememberPreferenceState(booleanPreferencesKey("mockFirstOpen"), false)

    if (!dialog) {
        AlertDialog(
            title = {
                Text("帮助")
            },
            text = {
                Column {
                    Text("当你点击发送后会调用词库里标签为[测试]的内容")
                }
            },
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = {
                    dialog = true
                }) {
                    Text("不再提示")
                }
            }
        )
    }

    SwipeRefresh(state = state, onRefresh = { refresh = true }) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().weight(9f)) {
                item {
                    messageList.forEach {
                        ListItem(
                            headlineContent = {
                                Row {
                                    Icon(
                                        imageVector = if (it.isBot) Icons.Outlined.AccountBox else Icons.Outlined.Call,
                                        contentDescription = ""
                                    )
                                    Text(if (it.isBot) "Bot" else "User")
                                }
                            },
                            modifier = Modifier.padding(vertical = 8.dp),
                            overlineContent = null,
                            supportingContent = {
                                Text(it.msg)
                            },
                            colors = ListItemDefaults.colors(),
                            tonalElevation = ListItemDefaults.Elevation,
                            shadowElevation = ListItemDefaults.Elevation
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(value = msg, onValueChange = {
                    msg = it
                })

                Button(onClick = {
                    messageList.add(MockMessage(false, msg))

                    CoroutineScope(Dispatchers.IO).launch {
                        DICList.INSTANCE.forEach {
                            val tr = TestRuntime(it.indexFile, msg, messageList)

                            try {
                                tr.invoke(msg)
                            } catch (e: Exception) {
                                messageList.add(MockMessage(true, e.message!! + "\n" + IOUtil.getException(e)))
                            }
                        }
                    }
                }) {
                    Text("发送")
                }
            }
        }
    }
}