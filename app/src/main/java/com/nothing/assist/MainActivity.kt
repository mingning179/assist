package com.nothing.assist

import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.nothing.assist.common.Log
import com.nothing.assist.common.PermissionUtils
import com.nothing.assist.processor.DataService
import com.nothing.assist.ui.theme.AssistTheme
import java.text.SimpleDateFormat

class MainActivity : ComponentActivity() {
    private var accessibilityOn = mutableStateOf(false)
    private lateinit var dataService: DataService
    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private var logText = mutableStateOf("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val accessibilityManager =
            this.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener { enabled -> accessibilityOn.value = enabled }
        accessibilityOn.value = accessibilityManager.isEnabled
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
        Column(modifier = modifier.padding(2.dp)) {
            var accessibilityColor = Color.Red
            if (accessibilityOn.value ) {
                accessibilityColor = Color.Green
            }
            Text(
                text = "无障碍: ${if (accessibilityOn.value) "已开启" else "已关闭"}",
                modifier = Modifier.fillMaxWidth().background(accessibilityColor),
                fontSize = TextUnit(40f, TextUnitType.Sp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)){
                Button(onClick = {
                    PermissionUtils.openAccessibility_settings(context)
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("无障碍")
                }
                Button(onClick = {
                    PermissionUtils.openRedmiBackgroundPopupSetting(context)
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("后台弹窗")
                }
                Button(onClick = {
                    PermissionUtils.openRedmiAutoStartSetting(context)
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("自启动")
                }
                Button(onClick = {
                    PermissionUtils.openBatteryOptimizationSetting(context)
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("省电策略")
                }
            }

            var signColor = Color.Red
            if (dataService.signCount > 0) {
                signColor = Color.Green
            }
            Text(
                text = "最近签到时间:\n ${
                    if (dataService.lastSignTime == null) "无" else sdf.format(dataService.lastSignTime)
                }\n\n今日签到次数:  ${if (dataService.signCount != -1) dataService.signCount else "未知"}",
                modifier = Modifier
                    .background(signColor)
                    .fillMaxWidth(),
                fontSize = TextUnit(18f, TextUnitType.Sp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Button(onClick = {
                    dataService.cleanData()
                    //刷新界面
                    flushContent()
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("清除数据")
                }
                Button(onClick = {
                    dataService.openApp(context, "com.myway.fxry")
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("在矫通")
                }
                Button(onClick = {
                    dataService.openApp(context, "com.tencent.mm")
                }, shape = AbsoluteRoundedCornerShape(8f)) {
                    Text("微信")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
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
                modifier = Modifier.background(Color(0xFF000000))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp)
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
                            modifier = Modifier.padding(2.dp),
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

    @Preview(showBackground = true, backgroundColor = 0xFFFF0000)
    @Composable
    fun GreetingPreview() {
        AssistTheme {
            Greeting("Android")
        }
    }
}