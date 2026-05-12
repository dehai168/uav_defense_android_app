package com.uav.defense.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uav.defense.data.models.PadTarget
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.AmberColor
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.RadarGreen
import com.uav.defense.ui.theme.TextMain

@Composable
fun ScanDialog(
    targets: List<PadTarget>,
    enabledTargetIds: Set<String>,
    relationFilter: String,
    selectedCategories: Set<String>,
    onClose: () -> Unit,
    onSetRelationFilter: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onSelectTarget: (String) -> Unit,
    onToggleEnabled: (String) -> Unit
) {
    val filtered = targets.filter {
        (relationFilter == "all" || it.relation == relationFilter) &&
            (selectedCategories.isEmpty() || it.typeLabel in selectedCategories)
    }

    MenuDialogFrame(title = "扫描结果", onClose = onClose) { bodyModifier ->
        Column(modifier = bodyModifier) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "全部", "hostile" to "敌对", "friendly" to "友好").forEach { (k, v) ->
                    FilterChip(
                        selected = relationFilter == k,
                        onClick = { onSetRelationFilter(k) },
                        label = { Text(v, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan,
                            labelColor = TextMain
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("无人机", "鸟类", "飞行器", "其他").forEach {
                    FilterChip(
                        selected = it in selectedCategories,
                        onClick = { onToggleCategory(it) },
                        label = { Text(it, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan,
                            labelColor = TextMain
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered) { t ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (t.relation == "hostile") DangerRed.copy(alpha = 0.08f) else AmberColor.copy(alpha = 0.06f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                            .clickable {
                                onSelectTarget(t.id)
                                onClose()
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = t.id in enabledTargetIds, onCheckedChange = { onToggleEnabled(t.id) })
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t.droneModel, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Tag(if (t.relation == "hostile") "敌" else "友", if (t.relation == "hostile") DangerRed else RadarGreen)
                                Tag(t.typeLabel, AccentCyan)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("${t.distance}km", color = TextMain.copy(alpha = 0.65f), fontSize = 10.sp)
                                Text("${t.altitude}m", color = TextMain.copy(alpha = 0.65f), fontSize = 10.sp)
                                Text("${t.speed}km/h", color = TextMain.copy(alpha = 0.65f), fontSize = 10.sp)
                                Text(t.detectedAt, color = TextMain.copy(alpha = 0.65f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 9.sp,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}
