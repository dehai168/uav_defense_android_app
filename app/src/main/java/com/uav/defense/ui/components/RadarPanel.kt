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

private const val RADAR_MAX_DISTANCE_KM = 1.5f
private const val RADAR_RING_STEP_KM = 0.125f
private const val RADAR_MAJOR_ANGLE_STEP = 30
private const val RADAR_MINOR_ANGLE_STEP = 15
private const val RADAR_SWEEP_TRAIL_STEPS = 18
private const val RADAR_SWEEP_TRAIL_STEP_DEGREES = 1.8f
private const val RADAR_SWEEP_PARTICLE_COUNT = 10

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
                val scale = min(size.width, size.height) * 0.85f / 2f / RADAR_MAX_DISTANCE_KM
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
            val scale = maxR / RADAR_MAX_DISTANCE_KM
            val labelAngle = Math.toRadians(32.0)
            val labelSin = kotlin.math.sin(labelAngle).toFloat()
            val labelCos = kotlin.math.cos(labelAngle).toFloat()

            for (ringIndex in 1..(RADAR_MAX_DISTANCE_KM / RADAR_RING_STEP_KM).toInt()) {
                val distanceKm = ringIndex * RADAR_RING_STEP_KM
                val radius = maxR * (distanceKm / RADAR_MAX_DISTANCE_KM)
                val isMajor = ringIndex % 4 == 0
                drawCircle(
                    color = RadarGreen.copy(alpha = if (isMajor) 0.34f else 0.14f),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(if (isMajor) 1.15f else 0.75f)
                )
                if (isMajor) {
                    val label = textMeasurer.measure(
                        "%.1f".format(distanceKm),
                        style = TextStyle(fontSize = 9.sp, color = RadarGreen.copy(alpha = 0.72f))
                    )
                    val labelRadius = (radius - 18f).coerceAtLeast(10f)
                    drawText(
                        textLayoutResult = label,
                        topLeft = Offset(
                            cx + labelRadius * labelSin - label.size.width / 2f,
                            cy - labelRadius * labelCos - label.size.height / 2f
                        )
                    )
                }
            }

            for (deg in 0 until 360 step RADAR_MAJOR_ANGLE_STEP) {
                val rad = Math.toRadians(deg.toDouble())
                val ex = cx + maxR * kotlin.math.sin(rad).toFloat()
                val ey = cy - maxR * kotlin.math.cos(rad).toFloat()
                drawLine(RadarGreen.copy(alpha = 0.2f), Offset(cx, cy), Offset(ex, ey), 0.95f)
            }

            for (deg in 0 until 360 step RADAR_MINOR_ANGLE_STEP) {
                val rad = Math.toRadians(deg.toDouble())
                val sinValue = kotlin.math.sin(rad).toFloat()
                val cosValue = kotlin.math.cos(rad).toFloat()
                val isMajor = deg % RADAR_MAJOR_ANGLE_STEP == 0
                val inner = if (isMajor) maxR + 2f else maxR + 5f
                val outer = if (isMajor) maxR + 13f else maxR + 10f
                drawLine(
                    color = if (isMajor) RadarGreen.copy(alpha = 0.96f) else RadarGreen.copy(alpha = 0.46f),
                    start = Offset(cx + inner * sinValue, cy - inner * cosValue),
                    end = Offset(cx + outer * sinValue, cy - outer * cosValue),
                    strokeWidth = if (isMajor) 1.45f else 0.85f
                )

                val labelDistance = if (isMajor) maxR + 25f else maxR + 22f
                val label = textMeasurer.measure(
                    "$deg",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = if (isMajor) RadarGreen.copy(alpha = 0.98f) else RadarGreen.copy(alpha = 0.58f),
                        fontWeight = if (isMajor) FontWeight.Bold else FontWeight.Normal
                    )
                )
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        cx + labelDistance * sinValue - label.size.width / 2f,
                        cy - labelDistance * cosValue - label.size.height / 2f
                    )
                )
            }

            for (step in RADAR_SWEEP_TRAIL_STEPS downTo 1) {
                val progress = 1f - (step - 1) / RADAR_SWEEP_TRAIL_STEPS.toFloat()
                val angle = radarSweepAngle - (step - 1) * RADAR_SWEEP_TRAIL_STEP_DEGREES
                val rad = Math.toRadians(angle.toDouble())
                val sinValue = kotlin.math.sin(rad).toFloat()
                val cosValue = kotlin.math.cos(rad).toFloat()
                val length = maxR * (0.62f + progress * 0.38f)
                val color = RadarGreen.copy(alpha = 0.04f + progress * 0.26f)

                drawLine(
                    color = color,
                    start = Offset(cx, cy),
                    end = Offset(cx + length * sinValue, cy - length * cosValue),
                    strokeWidth = 0.8f + progress * 1.2f
                )

                for (particle in 1..RADAR_SWEEP_PARTICLE_COUNT) {
                    val distanceRatio = particle / RADAR_SWEEP_PARTICLE_COUNT.toFloat()
                    val spread = ((particle % 3) - 1) * (1.4f + step * 0.15f)
                    val px = cx + length * distanceRatio * sinValue + spread * cosValue
                    val py = cy - length * distanceRatio * cosValue + spread * sinValue
                    drawCircle(
                        color = RadarGreen.copy(alpha = color.alpha * (0.45f + distanceRatio * 0.55f)),
                        radius = 0.9f + progress * 1.3f,
                        center = Offset(px, py)
                    )
                }
            }

            val sweep = Math.toRadians(radarSweepAngle.toDouble())
            val sweepEnd = Offset(
                cx + maxR * kotlin.math.sin(sweep).toFloat(),
                cy - maxR * kotlin.math.cos(sweep).toFloat()
            )
            drawLine(RadarGreen.copy(alpha = 0.94f), Offset(cx, cy), sweepEnd, 1.8f)
            repeat(12) { index ->
                val ratio = (index + 1) / 12f
                drawCircle(
                    color = RadarGreen.copy(alpha = 0.24f + ratio * 0.42f),
                    radius = 1.2f + ratio,
                    center = Offset(
                        cx + (sweepEnd.x - cx) * ratio,
                        cy + (sweepEnd.y - cy) * ratio
                    )
                )
            }

            targets.filter { it.id in enabledTargetIds }.forEach { target ->
                val point = targetToXY(target.bearing, target.distance, cx, cy, scale)
                val color = when {
                    target.relation == "hostile" -> HostileOrange
                    target.relation == "friendly" -> AmberColor
                    else -> NeutralOrange
                }
                for (tail in 1..3) {
                    val trailPoint = targetToXY(target.bearing - tail * 8f, target.distance, cx, cy, scale)
                    drawCircle(color.copy(alpha = 0.18f / tail), 3f, trailPoint)
                }
                drawCircle(color, 5f, point)
                if (selectedTargetId == target.id) drawCircle(AccentCyan, 8f, point, style = Stroke(1.5f))
                val label = textMeasurer.measure("T${target.id.takeLast(2)}", style = TextStyle(fontSize = 8.sp, color = color))
                drawText(textLayoutResult = label, topLeft = Offset(point.x + 6f, point.y - label.size.height / 2f))
            }

            val north = textMeasurer.measure("N", style = TextStyle(fontSize = 11.sp, color = RadarGreen, fontWeight = FontWeight.Bold))
            drawText(textLayoutResult = north, topLeft = Offset(cx - north.size.width / 2f, cy - maxR - 42f))
            drawCircle(RadarGreen, 4f, Offset(cx, cy))
        }

        Column(Modifier.align(Alignment.TopStart).padding(6.dp)) {
            Text("扫描周期: 3.2s", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("扫描方式: 环形", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text("天线方位角: ${"%.1f".format(radarSweepAngle)}°", color = RadarGreen.copy(alpha = 0.85f), fontSize = 11.sp)
            Text(currentTime, color = RadarGreen.copy(alpha = 0.70f), fontSize = 11.sp)
        }
        Column(Modifier.align(Alignment.TopEnd).padding(6.dp), horizontalAlignment = Alignment.End) {
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
