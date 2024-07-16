package com.nothing.assist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.nothing.assist.ui.theme.AssistTheme
import java.text.SimpleDateFormat

class MainActivity : ComponentActivity() {
    private var wza = mutableStateOf(false)
    private lateinit var dataService: DataService
    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private var logText = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val accessibilityManager =
            this.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener { enabled -> wza.value = enabled }
        wza.value = accessibilityManager.isEnabled
        dataService = DataService(this)
        flushContent()
    }

    override fun onResume() {
        super.onResume()
        flushContent()
    }

    private fun flushContent() {
        logText.value = Log.readLog()
        setContent {
            AssistTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val listState = rememberLazyListState()

        Column(modifier = modifier) {
            Text(
                text = "无障碍: ${if (wza.value) "已开启" else "已关闭"}",
                modifier = modifier,
                style = TextStyle.Default.copy(
                    color = if (wza.value) Color.Blue else Color.White,
                    background = if (wza.value) Color.Green else Color.Red,
                ),
                fontSize = TextUnit(60f, TextUnitType.Sp),
            )
            Button(onClick = {
                turnOnWza(context)
            }, shape = AbsoluteRoundedCornerShape(8f)) {
                Text("打开无障碍设置界面")
            }
            var color = Color.Red
            if (dataService.signCount > 0) {
                color = Color.Green
            }
            Text(
                text = "最近签到时间:\n\n ${
                    if (dataService.lastSignTime == null) "无" else sdf.format(dataService.lastSignTime)
                }\n\n今日签到次数:  ${if (dataService.signCount != -1) dataService.signCount else "未知"}",
                modifier = modifier
                    .background(color)
                    .width(Dp(5000f)),
                fontSize = TextUnit(25f, TextUnitType.Sp),
            )
            Button(onClick = {
                dataService.cleanData()
                //刷新界面
                flushContent()
            }, shape = AbsoluteRoundedCornerShape(8f)) {
                Text("清除数据")
            }

            Row {
                Button(onClick = {
                    dataService.openApp(context, "com.myway.fxry")
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("打开在矫通")
                }
                Button(onClick = {
                    dataService.openApp(context, "com.tencent.mm")
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("打开微信")
                }
            }

            Row {
                Button(onClick = {
                    // 加载日志
                    logText.value = Log.readLog()
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("加载日志")
                }
                Button(onClick = {
                    Log.clearLog()
                    // 清理日志后，刷新界面
                    flushContent()
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("清理日志")
                }
            }

            LazyColumn(
                state = listState,
                modifier = modifier
                    .background(Color(0xFF000000))
                    .width(Dp(5000f)),

                ) {
                val logs = logText.value.split("\n").reversed()
                items(logs.size) { index ->
                    val log = logs[index]
                    if (log.isNotEmpty()) {
                        val logParts = log.trim().split(" ", limit = 2)
                        val dateAndTag = logParts[0].trim()
                        val content = if (logParts.size > 1) logParts[1].trim() else ""
                        Text(
                            text = dateAndTag + "\n" + content,
                            modifier = modifier,
                            color = Color.White,
                            style = TextStyle.Default.copy(
                                fontSize = TextUnit(12f, TextUnitType.Sp),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun turnOnWza(context: Context) {
        // 引导用户到无障碍服务设置页面
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @Preview(showBackground = true, backgroundColor = 0xFFFF0000)
    @Composable
    fun GreetingPreview() {
        AssistTheme {
            Greeting("Android")
        }
    }
}