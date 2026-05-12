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

            drawLine(RadarGreen.copy(alpha = 0.3f), Offset(cx - maxR, cy), Offset(cx + maxR, cy), 1f)
            drawLine(RadarGreen.copy(alpha = 0.3f), Offset(cx, cy - maxR), Offset(cx, cy + maxR), 1f)

            val radialMarks = listOf(0.5f, 1.0f, 1.5f)
            radialMarks.forEach { km ->
                val r = maxR * (km / 1.5f)
                val textStyle = TextStyle(fontSize = 10.sp, color = RadarGreen.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold)
                val rightLabel = textMeasurer.measure(km.toString(), style = textStyle)
                val topLabel = textMeasurer.measure(km.toString(), style = textStyle)
                drawText(rightLabel, topLeft = Offset(cx + r + 4f, cy - rightLabel.size.height / 2f))
                drawText(topLabel, topLeft = Offset(cx - topLabel.size.width / 2f, cy - r - topLabel.size.height - 2f))
            }

            for (deg in 0 until 360 step 45) {
                val rad = Math.toRadians(deg.toDouble())
                val ex = cx + maxR * kotlin.math.sin(rad).toFloat()
                val ey = cy - maxR * kotlin.math.cos(rad).toFloat()
                drawLine(RadarGreen.copy(alpha = 0.15f), Offset(cx, cy), Offset(ex, ey), 0.8f)
            }

            drawCircle(RadarGreen.copy(alpha = 0.65f), maxR, Offset(cx, cy), style = Stroke(1.5f))
            for (i in 1..3) {
                val r = maxR * (i / 3f)
                drawCircle(RadarGreen.copy(alpha = 0.25f), r, Offset(cx, cy), style = Stroke(1f))
            }

            for (deg in 0 until 360 step 15) {
                val rad = Math.toRadians(deg.toDouble())
                val sinValue = kotlin.math.sin(rad).toFloat()
                val cosValue = kotlin.math.cos(rad).toFloat()
                val inner = if (deg % 30 == 0) maxR + 2f else maxR + 5f
                val outer = if (deg % 30 == 0) maxR + 12f else maxR + 9f
                drawLine(
                    color = if (deg % 30 == 0) RadarGreen.copy(alpha = 0.95f) else RadarGreen.copy(alpha = 0.45f),
                    start = Offset(cx + inner * sinValue, cy - inner * cosValue),
                    end = Offset(cx + outer * sinValue, cy - outer * cosValue),
                    strokeWidth = if (deg % 30 == 0) 1.4f else 0.8f
                )
                if (deg % 30 == 0) {
                    val lx = cx + (maxR + 22f) * sinValue
                    val ly = cy - (maxR + 22f) * cosValue
                    val t = textMeasurer.measure(
                        "$deg",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = RadarGreen.copy(alpha = 0.98f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = t,
                        topLeft = Offset(lx - t.size.width / 2f, ly - t.size.height / 2f)
                    )
                }
            }

            // Gradient trail: drawn BEFORE sweep line so it sits behind it.
            // 40 thin arc slices going from transparent at the trailing end to green just
            // before the sweep line. All slices occupy angles BEFORE the sweep angle.
            val trailTotalAngle = 48f
            val trailSteps = 40
            val stepAngle = trailTotalAngle / trailSteps
            val sliceOverlap = 0.3f  // slight overlap between slices to prevent visible gaps
            for (step in 0 until trailSteps) {
                val progress = step.toFloat() / trailSteps
                val alpha = progress * 0.5f
                drawArc(
                    color = RadarGreen.copy(alpha = alpha),
                    startAngle = radarSweepAngle - 90f - trailTotalAngle + step * stepAngle,
                    sweepAngle = stepAngle + sliceOverlap,
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
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(p.x + 6f, p.y - label.size.height / 2f)
                )
            }

            val n = textMeasurer.measure("N", style = TextStyle(fontSize = 11.sp, color = RadarGreen, fontWeight = FontWeight.Bold))
            drawText(
                textLayoutResult = n,
                topLeft = Offset(cx - n.size.width / 2f, cy - maxR - 40f)
            )
            drawCircle(RadarGreen, 4f, Offset(cx, cy))
        }

        Column(Modifier.align(Alignment.TopStart).padding(6.dp)) {
            Text("扫描周期: 3.2s", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("扫描方式: 环形", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("天线方位角: ${"%.1f".format(radarSweepAngle)}°", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text(currentTime, color = RadarGreen.copy(alpha = 0.70f), fontSize = 11.sp)
        }
        Column(Modifier.align(Alignment.TopEnd).padding(6.dp), horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text("连接状态: 已连接", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("工作模式: 搜索", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(6.dp)) {
            Text("一体机: 联动在线", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("连接状态: 已连接", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("工作模式: 搜索", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("工作状态: 正常工作", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
        }
    }
}
