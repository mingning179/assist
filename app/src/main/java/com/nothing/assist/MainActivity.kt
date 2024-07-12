package com.nothing.assist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
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

        LazyColumn(state = listState, modifier = modifier) {
            item {
                Text(
                    text = "无障碍状态: ${if (wza.value) "开启" else "关闭"}",
                    modifier = modifier,
                )
                Button(onClick = {
                    turnOnWza(context)
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    if (wza.value.not()) {
                        Text("开启无障碍")
                    } else {
                        Text("关闭无障碍")
                    }
                }
                Text(
                    text = "最近签到时间: ${
                        if (dataService.lastSignTime == null) "无" else sdf.format(dataService.lastSignTime)
                    }",
                    modifier = modifier,
                )
                Text(
                    text = "今日签到次数: ${if (dataService.signCount != -1) dataService.signCount else "未知"}",
                    modifier = modifier,
                )
                Button(onClick = {
                    dataService.cleanData()
                    //刷新界面
                    flushContent()
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("清除数据")
                }
                Button(onClick = {
                    dataService.openApp(context)
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("打开目标APP")
                }

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

            val logs = logText.value.split("\n")
            items(logs.size) { index ->
                val log = logs[index]
                if (log.isNotEmpty()) {
                    val logParts = log.split(" ", limit = 2)
                    val dateAndTag = logParts[0]
                    val content = if (logParts.size > 1) logParts[1] else ""
                    Text(
                        text = dateAndTag+"\n"+content,
                        modifier = modifier,
                    )
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