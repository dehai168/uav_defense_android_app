package com.uav.defense.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.uav.defense.data.models.PadTarget
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.PanelBg
import com.uav.defense.ui.theme.TextMain

@Composable
fun PlaybackDialog(targets: List<PadTarget>, onClose: () -> Unit) {
    val timeline = listOf("14:21发现目标", "14:24建立跟踪", "14:27目标盘旋", "14:30进入警戒圈", "14:33联动处置")
    val hostile = targets.filter { it.relation == "hostile" }

    Dialog(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth(0.6f).background(PanelBg, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("历史回放", color = AccentCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = TextMain) }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                timeline.forEach { text ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(88.dp)) {
                        Spacer(Modifier.height(2.dp))
                        Spacer(Modifier.width(1.dp).height(8.dp).background(AccentCyan, CircleShape))
                        Text(text, color = TextMain.copy(alpha = 0.8f), fontSize = 9.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(hostile) {
                    Column(Modifier.fillMaxWidth().background(DangerRed.copy(alpha = 0.08f), RoundedCornerShape(6.dp)).border(1.dp, BorderColor, RoundedCornerShape(6.dp)).padding(8.dp)) {
                        Text("${it.detectedAt}  ${it.droneModel}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(it.trajectoryPrediction, color = TextMain.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
