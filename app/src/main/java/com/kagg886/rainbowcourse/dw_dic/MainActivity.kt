package com.kagg886.rainbowcourse.dw_dic

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kagg886.rainbowcourse.dw_dic.ui.page.PageConfig
import com.kagg886.rainbowcourse.dw_dic.ui.theme.DW_DICTheme
import com.kagg886.rainbowcourse.dw_dic.ui.theme.Typography
import com.kagg886.rainbowcourse.dw_dic.util.DialogSetting
import com.kagg886.rainbowcourse.dw_dic.util.Promise
import com.kagg886.rainbowcourse.dw_dic.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val fileOpener: Promise<Intent, ActivityResult> by lazy {
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            fileOpener.resolve(it)
        }

        Promise {
            launcher.launch(it)
        }
    }

    lateinit var dialogOpener: Promise<DialogSetting, String>

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileOpener.run {
            Logger.i("文件选择器初始化完成!")
        }
        setContent {
            DW_DICTheme {
                val nav = rememberNavController()
                var router by remember {
                    mutableStateOf(PageConfig.DEFAULT_ROUTER)
                }

                LaunchedEffect(router) {
                    nav.navigate(router)
                }

                var dialog by remember {
                    mutableStateOf(false)
                }

                var setting by remember {
                    mutableStateOf(DialogSetting())
                }

                var str by remember {
                    mutableStateOf("")
                }

                if (dialog) {
                    fun dismiss() {
                        dialogOpener.resolve(str)
                        setting = DialogSetting()
                        dialog = false
                    }
                    AlertDialog(
                        title = {
                            Text(setting.title)
                        },
                        text = {
                            OutlinedTextField(value = str, onValueChange = { str = it }, label = {
                                Text(setting.placeholder)
                            })
                        },
                        onDismissRequest = {
                            dismiss()
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dismiss()
                                }
                            ) {
                                Text("Confirm")
                            }
                        }
                    )
                }

                dialogOpener = remember {
                    Promise {
                        str = it!!.text
                        setting = it
                        dialog = true
                    }
                }

                Scaffold(bottomBar = {
                    NavigationBar {
                        PageConfig.list.forEach { entry ->
                            NavigationBarItem(
                                icon = {
                                    Icon(imageVector = entry.icon, "")
                                },
                                label = {
                                    Text(entry.title)
                                },
                                selected = entry.router == router,
                                onClick = {
                                    router = entry.router
                                },
                                alwaysShowLabel = false
                            )
                        }
                    }
                }, topBar = {
                    TopAppBar(title = {
                        Column {
                            Text(resources.getString(R.string.app_name), style = Typography.titleLarge)
                            Text(PageConfig.findPageByRouter(router).title, style = Typography.titleMedium)
                        }
                    })
                }) {
                    NavHost(
                        navController = nav,
                        startDestination = PageConfig.DEFAULT_ROUTER,
                        modifier = Modifier.padding(it).fillMaxSize()
                    ) {
                        PageConfig.list.forEach { entry ->
                            composable(entry.router) {
                                entry.widget()
                            }
                        }
                    }
                }
            }
        }

    }
}