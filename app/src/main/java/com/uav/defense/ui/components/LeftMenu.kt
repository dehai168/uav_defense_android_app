package com.uav.defense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.PanelBg
import com.uav.defense.ui.theme.TextMain

data class MenuItem(val label: String, val icon: ImageVector, val modal: String)

private val menuItems = listOf(
    MenuItem("扫描", Icons.Default.List, "scan"),
    MenuItem("管理", Icons.Default.Settings, "manage"),
    MenuItem("回放", Icons.Default.History, "playback"),
    MenuItem("分析", Icons.Default.BarChart, "analysis")
)

private val settingItem = MenuItem("设置", Icons.Default.Tune, "settings")

@Composable
fun LeftMenu(activeModal: String?, onMenuClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxHeight().width(72.dp).background(PanelBg).border(1.dp, BorderColor).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        menuItems.forEach {
            MenuButton(it, activeModal == it.modal) { onMenuClick(it.modal) }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        MenuButton(settingItem, activeModal == settingItem.modal) { onMenuClick(settingItem.modal) }
    }
}

@Composable
private fun MenuButton(item: MenuItem, active: Boolean, onClick: () -> Unit) {
    val tint = if (active) AccentCyan else TextMain.copy(alpha = 0.75f)
    Column(
        modifier = Modifier.fillMaxWidth().background(if (active) AccentCyan.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(item.icon, contentDescription = item.label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(item.label, color = tint, fontSize = 10.sp)
    }
}
