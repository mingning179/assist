package com.nothing.assist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.nothing.assist.ui.theme.AssistTheme
import java.text.SimpleDateFormat


class MainActivity : ComponentActivity() {
    var wza = mutableStateOf(false)
    var dataService: DataService? = null
    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val accessibilityManager =
            this.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener { enabled -> wza.value = enabled }
        wza.value = accessibilityManager.isEnabled
        dataService = DataService(this)
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
        Column(modifier = modifier) {
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
                    if (dataService?.lastSignTime == null) "无" else sdf.format(dataService?.lastSignTime)
                }",
                modifier = modifier,
            )
            Text(
                text = "今日签到次数: ${if (dataService?.signCount != -1) dataService?.signCount else "未知"}",
                modifier = modifier,
            )
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

