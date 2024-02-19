package com.kagg886.rainbowcourse.dw_dic.ui.page.impl

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kagg886.rainbowcourse.dw_dic.EditActivity
import com.kagg886.rainbowcourse.dw_dic.App
import com.kagg886.rainbowcourse.dw_dic.MainActivity
import com.kagg886.rainbowcourse.dw_dic.R
import com.kagg886.rainbowcourse.dw_dic.ui.theme.Typography
import com.kagg886.rainbowcourse.dw_dic.util.*
import io.github.seikodictionaryenginev2.base.env.DICList
import io.github.seikodictionaryenginev2.base.env.DictionaryEnvironment
import io.github.seikodictionaryenginev2.base.model.DICParseResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.*

@Composable
fun HomePage() {

    //--------刷新
    var refresh by remember {
        mutableStateOf(true)
    }

    val state = rememberSwipeRefreshState(refresh)

    //---------词库刷新结果
    var result by remember {
        mutableStateOf<List<DICParseResult>>(ArrayList())
    }
    LaunchedEffect(refresh) {
        result = DICList.INSTANCE.refresh()
        Logger.i("加载完成!词库数目:${result.size}")
        result.filter { !it.success }.forEach {
            Logger.w("词库:${it.dicName} 加载出错!", it.err)
        }
        refresh = false
    }

    //---------SnackBar
    val hostState = remember {
        SnackbarHostState()
    }
    val cScope = rememberCoroutineScope()

    var littleFloating by remember {
        mutableStateOf(false)
    }
    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (littleFloating) {
                    SmallFloatingActionButton(onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            val result2 = (App.currentActivity() as MainActivity).fileOpener.startForResult(
                                Intent(Intent.ACTION_GET_CONTENT)
                                    .addCategory(Intent.CATEGORY_OPENABLE)
                                    .setType("*/*")
                            )
                            if (result2.resultCode != Activity.RESULT_OK) {
                                hostState.showSnackbar("操作被取消了!")
                                return@launch
                            }
                            val str = (App.currentActivity() as MainActivity).dialogOpener.startForResult(
                                DialogSetting("设置词库名称", "输入名称", UUID.randomUUID().toString() + ".seiko")
                            )
                            Logger.i("开始导入词库:$str")

                            (App.currentActivity().contentResolver.openInputStream(result2.data?.data!!)!!).use {
                                Paths.get(DictionaryEnvironment.getInstance().dicRoot.absolutePath, str).toFile()
                                    .writeText(it.reader(StandardCharsets.UTF_8).readText())
                            }
                            hostState.showSnackbar("导入成功，请下拉刷新以正确加载词库!")
                        }
                    }) {
                        Icon(Icons.Outlined.Search, "search")
                    }
                    SmallFloatingActionButton(onClick ={
                        App.currentActivity().startActivity(Intent(App.currentActivity(),
                            EditActivity::class.java).also {
                            it.putExtra("exist_file",false)
                        })
                    }) {
                        Icon(Icons.Outlined.Add, "add")
                    }
                    SmallFloatingActionButton(onClick = {
                        openURL("https://seikodictionaryenginev2.github.io/dist")
                    }) {
                        Icon(painter = painterResource(R.drawable.baseline_help_outline_24), "help")
                    }
                }
                FloatingActionButton(
                    onClick = {
                        littleFloating = !littleFloating
                    },
                ) {
                    Icon(Icons.Outlined.Menu, "Floating action button.")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = hostState)
        }
    ) {
        SwipeRefresh(state = state, onRefresh = { refresh = true }, modifier = Modifier.padding(it)) {
            if (result.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("暂无词库，建议下拉刷新一下喔")
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                result.forEach { project ->
                    item(project.dicName) {
                        DICItem(project, cScope, hostState)
                    }
                }
            }


        }
    }


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DICItem(project: DICParseResult, scope: CoroutineScope, host: SnackbarHostState) {
    var dialog by remember {
        mutableStateOf(false)
    }

    val loaded = project.success
    var enabled by remember {
        val enabled = DictionaryEnvironment.getInstance().getSettingByName(project.dicName).isEnabled

        updateDicConfig(project.dicName,loaded && enabled)
        mutableStateOf(
            loaded && enabled
        )
    }

    if (!project.success && dialog) {
        AlertDialog(onDismissRequest = { dialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = project.dicName + "加载失败!\n" + project.err.message,
                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    var optionCard by remember {
        mutableStateOf(false)
    }

    if (optionCard) {
        AlertDialog(
            title = {
                Text("管理")
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ListItem(headlineContent = {
                        TextButton(onClick = {
                            val ctx = App.currentActivity()
                            ctx.startActivity(Intent(ctx,
                                EditActivity::class.java).also {
                                it.putExtra("exist_file", true);
                                it.putExtra("filename", DICList.INSTANCE.findProjectByName(project.dicName).indexFile.file.absolutePath);
                            })
                            optionCard = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("编辑词库", style = Typography.bodyLarge)
                        }
                    })
                    ListItem(headlineContent = {
                        TextButton(onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                Paths.get(DictionaryEnvironment.getInstance().dicRoot.absolutePath, project.dicName)
                                    .toFile().deleteDir()
                                host.showSnackbar("删除完毕!,下拉刷新以查看效果!")
                            }
                            optionCard = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("删除词库", style = Typography.bodyLarge)
                        }
                    })
                }
            },
            onDismissRequest = {
                optionCard = false
            },
            confirmButton = {}
        )
    }

    ElevatedCard(elevation = CardDefaults.cardElevation(
        defaultElevation = 6.dp
    ), modifier = Modifier.padding(30.dp).fillMaxWidth(0.9f).height(120.dp).combinedClickable(
        onLongClick = {
            optionCard = true
        },
        onClick = {
            if (!project.success) {
                dialog = true
            }
        }
    )) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val a = if (!project.success) Icons.Outlined.Warning else Icons.Outlined.Edit
                Icon(imageVector = a, contentDescription = "")
                Text(
                    text = project.dicName,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp).fillMaxWidth(0.8f),
                    textAlign = TextAlign.Center,
                )
            }

            Switch(onCheckedChange = {
                if (!enabled) {
                    try {
                        DICList.INSTANCE.findProjectByName(project.dicName).init()
                        Logger.i("启用词库:${project.dicName}成功!")
                    } catch (e: Exception) {
                        enabled = false
                        Logger.w("启用词库:${project.dicName}失败!", e)
                        scope.launch {
                            host.showSnackbar("单击图标以获得错误详情，修复词库后再单击开关以启用!\n长按条目以编辑/删除词库!")
                        }
                        return@Switch
                    }
                    enabled = true
                } else {
                    enabled = false
                }
                updateDicConfig(project.dicName, enabled)
            }, checked = enabled)
        }
    }
}

fun updateDicConfig(name: String, boolean: Boolean) {
    val setting = DictionaryEnvironment.getInstance().getSettingByName(name)
    setting.isEnabled = boolean
    DictionaryEnvironment.getInstance().dicConfig.put(name, setting)
    DictionaryEnvironment.getInstance().dicConfig.save()
}