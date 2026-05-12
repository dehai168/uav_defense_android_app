package com.uav.defense.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.TextMain
import androidx.compose.material3.Text

@Composable
fun SettingsDialog(onClose: () -> Unit, onToast: (String) -> Unit) {
    val items = listOf(
        "离线地图设置" to "离线地图设置: 切换离线底图包与缓存策略",
        "白名单设置" to "白名单设置: 维护友好目标与巡检航线白名单",
        "警报设置" to "警报设置: 配置分级告警、提示音和通知规则",
        "OTA 管理" to "OTA 管理: 查看设备固件版本和升级状态",
        "日志管理" to "日志管理: 导出演示日志和系统事件记录"
    )

    MenuDialogFrame(title = "系统设置", onClose = onClose) { bodyModifier ->
        Column(modifier = bodyModifier) {
            items.forEach { (title, toast) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToast(toast) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = TextMain, fontSize = 14.sp)
                }
                Spacer(
                    Modifier
                        .width(1.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderColor.copy(alpha = 0.5f))
                )
            }
        }
    }
}
