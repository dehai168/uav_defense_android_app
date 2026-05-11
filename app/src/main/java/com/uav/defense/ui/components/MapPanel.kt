package com.uav.defense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IconButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uav.defense.data.models.PadTarget
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.AmberColor
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.PanelBg
import com.uav.defense.ui.theme.RadarGreen
import com.uav.defense.ui.theme.TextMain
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

@Composable
fun MapPanel(
    targets: List<PadTarget>,
    enabledTargetIds: Set<String>,
    selectedTargetId: String?,
    radarSweepAngle: Float,
    currentMapMode: String,
    measureMode: Boolean,
    onTargetClick: (String) -> Unit,
    onSwitchMapMode: () -> Unit,
    onToggleMeasure: () -> Unit,
    onMarkFalsePositive: (String) -> Unit,
    onSendCommand: (String, String) -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var measurePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var measureDistance by remember { mutableStateOf<Double?>(null) }
    var pulse by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            pulse = (pulse + 0.06f) % (2f * PI.toFloat())
        }
    }

    Box(modifier = modifier.background(PanelBg)) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(measureMode, targets, enabledTargetIds) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onTap = { tap ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val scale = (min(size.width, size.height) / 2f) / 1.5f
                        if (measureMode) {
                            when (measurePoints.size) {
                                0 -> measurePoints = listOf(tap)
                                1 -> {
                                    measurePoints = measurePoints + tap
                                    measureDistance = distanceKm(measurePoints[0], tap, scale)
                                }
                                else -> {
                                    measurePoints = listOf(tap)
                                    measureDistance = null
                                }
                            }
                        } else {
                            val hit = targets.filter { it.id in enabledTargetIds }.firstOrNull {
                                val pos = targetToXY(it.bearing, it.distance, centerX, centerY, scale)
                                (tap - pos).getDistance() < 28f
                            }
                            onTargetClick(hit?.id ?: "")
                        }
                    }
                )
            }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val scale = (min(w, h) / 2f) / 1.5f

            val bg = when (currentMapMode) {
                "标准图" -> Color(0xFF0D1F0D)
                "暗色图" -> Color(0xFF050505)
                else -> PanelBg
            }
            drawRect(bg)

            var gx = 0f
            while (gx <= w) {
                drawLine(BorderColor.copy(alpha = 0.35f), Offset(gx, 0f), Offset(gx, h), strokeWidth = 0.6f)
                gx += 60f
            }
            var gy = 0f
            while (gy <= h) {
                drawLine(BorderColor.copy(alpha = 0.35f), Offset(0f, gy), Offset(w, gy), strokeWidth = 0.6f)
                gy += 60f
            }

            for (i in 1..3) drawCircle(AccentCyan.copy(alpha = 0.2f), scale * i * 0.5f, Offset(cx, cy), style = Stroke(1f))

            drawArc(
                color = RadarGreen.copy(alpha = 0.16f),
                startAngle = radarSweepAngle - 90f,
                sweepAngle = 30f,
                useCenter = true,
                topLeft = Offset(cx - scale * 1.5f, cy - scale * 1.5f),
                size = Size(scale * 3f, scale * 3f)
            )

            drawCircle(AccentCyan.copy(alpha = 0.8f), 20f, Offset(cx, cy))
            drawCircle(AccentCyan.copy(alpha = 0.3f), 30f, Offset(cx, cy), style = Stroke(2f))

            val pulseScale = (sin(pulse) * 0.3f + 0.7f)
            targets.filter { it.id in enabledTargetIds }.forEach { t ->
                val pos = targetToXY(t.bearing, t.distance, cx, cy, scale)
                val color = if (t.relation == "hostile") DangerRed else AmberColor
                val r = if (t.relation == "hostile") 10f * pulseScale else 8f
                if (t.relation == "hostile") drawCircle(color.copy(alpha = 0.3f), r * 2f, pos)
                drawCircle(color, r, pos)
                if (selectedTargetId == t.id) drawCircle(AccentCyan, r + 4f, pos, style = Stroke(2f))
                val label = textMeasurer.measure("目标${t.id.takeLast(2)}", style = TextStyle(fontSize = 10.sp, color = color))
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(pos.x - label.size.width / 2f, pos.y - r - 18f)
                )
            }

            if (measureMode && measurePoints.isNotEmpty()) {
                drawCircle(AccentCyan, 6f, measurePoints[0])
                if (measurePoints.size == 2) {
                    drawLine(AccentCyan, measurePoints[0], measurePoints[1], 2f)
                    drawCircle(AccentCyan, 6f, measurePoints[1])
                    measureDistance?.let {
                        val text = textMeasurer.measure("%.2fkm".format(it), style = TextStyle(color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        val mid = Offset((measurePoints[0].x + measurePoints[1].x) / 2f, (measurePoints[0].y + measurePoints[1].y) / 2f)
                        drawText(
                            textLayoutResult = text,
                            topLeft = Offset(mid.x - text.size.width / 2f, mid.y - 20f)
                        )
                    }
                }
            }

            val centerText = textMeasurer.measure("雷", style = TextStyle(fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold))
            drawText(
                textLayoutResult = centerText,
                topLeft = Offset(cx - centerText.size.width / 2f, cy - centerText.size.height / 2f)
            )
        }

        Box(Modifier.align(Alignment.TopEnd).padding(8.dp).background(PanelBg.copy(alpha = 0.86f), RoundedCornerShape(4.dp)).border(1.dp, BorderColor, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("SAT-MAP | $currentMapMode", color = AccentCyan, fontSize = 10.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(PanelBg.copy(alpha = 0.86f), RoundedCornerShape(4.dp)).border(1.dp, BorderColor, RoundedCornerShape(4.dp)).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onSwitchMapMode, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Layers, null, tint = AccentCyan, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = onToggleMeasure, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Straighten, null, tint = if (measureMode) AccentCyan else TextMain, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.GpsFixed, null, tint = TextMain, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ZoomIn, null, tint = TextMain, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ZoomOut, null, tint = TextMain, modifier = Modifier.size(16.dp)) }
        }

        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
            Text("近程雷达-01 · 1.5km", color = AccentCyan.copy(alpha = 0.7f), fontSize = 10.sp)
        }

        targets.find { it.id == selectedTargetId }?.let { target ->
            TargetPopup(target, onDismiss = { onTargetClick("") }, onMarkFalsePositive = onMarkFalsePositive, onSendCommand = onSendCommand)
        }
    }
}

@Composable
private fun TargetPopup(
    target: PadTarget,
    onDismiss: () -> Unit,
    onMarkFalsePositive: (String) -> Unit,
    onSendCommand: (String, String) -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = PanelBg)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(target.droneModel, color = if (target.relation == "hostile") DangerRed else AmberColor, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = TextMain, modifier = Modifier.size(16.dp)) }
                }
                val lines = listOf(
                    "机型" to target.droneModel,
                    "敌友" to if (target.relation == "hostile") "敌对" else "友好",
                    "类型" to target.typeLabel,
                    "经纬度" to "%.4f, %.4f".format(target.lng, target.lat),
                    "距离" to "${target.distance}km",
                    "高度" to "${target.altitude}m",
                    "速度" to "${target.speed}km/h",
                    "方位角" to "${target.bearing.toInt()}°",
                    "ETA" to target.eta,
                    "俯仰角" to "${target.pitch}°",
                    "垂直速度" to "${target.verticalSpeed}m/s"
                )
                lines.forEach { (l, v) -> Text("$l：$v", color = TextMain, fontSize = 11.sp) }
                if (target.actionStatus.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(target.actionStatus, color = AccentCyan, fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PopupAction("误报", AmberColor) { onMarkFalsePositive(target.id) }
                    if (target.relation == "hostile") {
                        PopupAction("驱离", AccentCyan) { onSendCommand(target.id, "驱离") }
                        PopupAction("击落", DangerRed) { onSendCommand(target.id, "击落") }
                        PopupAction("控制", RadarGreen) { onSendCommand(target.id, "控制") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupAction(label: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) { Text(label, fontSize = 10.sp) }
}
