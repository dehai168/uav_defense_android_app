package com.uav.defense.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.uav.defense.ui.theme.BackgroundDark
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.PanelBg
import com.uav.defense.ui.theme.RadarGreen
import com.uav.defense.ui.theme.TextMain

@Composable
fun AnalysisDialog(targets: List<PadTarget>, onClose: () -> Unit) {
    val timeData = listOf("14:00-14:15" to 2, "14:15-14:30" to 5, "14:30-14:45" to 4, "14:45-15:00" to 1)
    val maxTime = timeData.maxOf { it.second }

    val typeData = targets.groupBy { it.typeLabel }.mapValues { it.value.size }
    val maxType = typeData.values.maxOrNull() ?: 1

    Dialog(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth(0.6f).background(PanelBg, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("统计分析", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = TextMain) }
            }
            Spacer(Modifier.height(10.dp))

            Card(colors = CardDefaults.cardColors(containerColor = BackgroundDark), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("时间段分布", color = AccentCyan, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    timeData.forEach { (label, count) ->
                        BarRow(label, count, maxTime, AccentCyan)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Card(colors = CardDefaults.cardColors(containerColor = BackgroundDark), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("类型分布", color = RadarGreen, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    typeData.forEach { (label, count) ->
                        BarRow(label, count, maxType, RadarGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun BarRow(label: String, count: Int, max: Int, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextMain.copy(alpha = 0.7f), fontSize = 10.sp, modifier = Modifier.width(92.dp))
        Box(Modifier.weight(1f).height(16.dp).background(BorderColor, RoundedCornerShape(3.dp))) {
            Box(Modifier.fillMaxWidth(count.toFloat() / max).height(16.dp).background(color, RoundedCornerShape(3.dp)))
        }
        Text("$count", color = TextMain, fontSize = 11.sp, modifier = Modifier.width(20.dp))
    }
}
