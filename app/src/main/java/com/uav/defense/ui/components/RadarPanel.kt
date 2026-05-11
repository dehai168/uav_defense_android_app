package com.uav.defense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.uav.defense.ui.theme.HostileOrange
import com.uav.defense.ui.theme.NeutralOrange
import com.uav.defense.ui.theme.RadarGreen
import kotlin.math.min

@Composable
fun RadarPanel(
    targets: List<PadTarget>,
    enabledTargetIds: Set<String>,
    radarSweepAngle: Float,
    currentTime: String,
    selectedTargetId: String?,
    onTargetClick: (String) -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.background(Color(0xFF000308)).pointerInput(targets, enabledTargetIds) {
        detectTapGestures(
            onDoubleTap = { onDoubleTap() },
            onTap = { tap ->
                val cx = size.width / 2f
                val cy = size.height / 2f
                val scale = min(size.width, size.height) * 0.85f / 2f / 1.5f
                val hit = targets.filter { it.id in enabledTargetIds }.firstOrNull {
                    val p = targetToXY(it.bearing, it.distance, cx, cy, scale)
                    (tap - p).getDistance() < 20f
                }
                if (hit != null) onTargetClick(hit.id)
            }
        )
    }) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = min(size.width, size.height) * 0.85f / 2f
            val scale = maxR / 1.5f

            drawCircle(RadarGreen.copy(alpha = 0.65f), maxR, Offset(cx, cy), style = Stroke(1.5f))
            for (i in 1..3) {
                val r = maxR * (i / 3f)
                drawCircle(RadarGreen.copy(alpha = 0.25f), r, Offset(cx, cy), style = Stroke(1f))
                val t = textMeasurer.measure("${i * 0.5}km", style = TextStyle(fontSize = 8.sp, color = RadarGreen.copy(alpha = 0.5f)))
                drawText(t, Offset(cx + 3f, cy - r - t.size.height))
            }

            for (deg in 0 until 360 step 15) {
                val rad = Math.toRadians(deg.toDouble())
                val lx = cx + (maxR + 14f) * kotlin.math.sin(rad).toFloat()
                val ly = cy - (maxR + 14f) * kotlin.math.cos(rad).toFloat()
                val t = textMeasurer.measure("$deg", style = TextStyle(fontSize = 7.sp, color = RadarGreen.copy(alpha = 0.5f)))
                drawText(t, Offset(lx - t.size.width / 2f, ly - t.size.height / 2f))
            }

            for (i in 0..5) {
                drawArc(
                    color = RadarGreen.copy(alpha = (6 - i) / 6f * 0.4f),
                    startAngle = radarSweepAngle - 90f - i * 8f,
                    sweepAngle = 8f,
                    useCenter = true,
                    topLeft = Offset(cx - maxR, cy - maxR),
                    size = Size(maxR * 2, maxR * 2)
                )
            }

            val sweep = Math.toRadians(radarSweepAngle.toDouble())
            drawLine(RadarGreen.copy(alpha = 0.9f), Offset(cx, cy), Offset(cx + maxR * kotlin.math.sin(sweep).toFloat(), cy - maxR * kotlin.math.cos(sweep).toFloat()), 1.5f)

            targets.filter { it.id in enabledTargetIds }.forEach { t ->
                val p = targetToXY(t.bearing, t.distance, cx, cy, scale)
                val c = when {
                    t.relation == "hostile" -> HostileOrange
                    t.relation == "friendly" -> AmberColor
                    else -> NeutralOrange
                }
                for (tail in 1..3) {
                    val tp = targetToXY(t.bearing - tail * 8f, t.distance, cx, cy, scale)
                    drawCircle(c.copy(alpha = 0.2f / tail), 3f, tp)
                }
                drawCircle(c, 5f, p)
                if (selectedTargetId == t.id) drawCircle(AccentCyan, 8f, p, style = Stroke(1.5f))
                val label = textMeasurer.measure("T${t.id.takeLast(2)}", style = TextStyle(fontSize = 8.sp, color = c))
                drawText(label, Offset(p.x + 6f, p.y - label.size.height / 2f))
            }

            val n = textMeasurer.measure("N", style = TextStyle(fontSize = 11.sp, color = RadarGreen, fontWeight = FontWeight.Bold))
            drawText(n, Offset(cx - n.size.width / 2f, cy - maxR - 26f))
            drawCircle(RadarGreen, 4f, Offset(cx, cy))
        }

        Column(Modifier.align(Alignment.TopStart).padding(6.dp)) {
            Text("扫描周期: 3.2s | 扫描方式: 环形 | 天线方位角: ${radarSweepAngle.toInt()}° | $currentTime", color = RadarGreen.copy(alpha = 0.75f), fontSize = 8.sp)
        }
        Column(Modifier.align(Alignment.TopEnd).padding(6.dp)) {
            Text("连接状态: 已连接 | 工作模式: 搜索", color = RadarGreen.copy(alpha = 0.75f), fontSize = 8.sp)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(6.dp)) {
            Text("一体机: 联动在线 | 工作状态: 正常工作", color = RadarGreen.copy(alpha = 0.75f), fontSize = 8.sp)
        }
    }
}
