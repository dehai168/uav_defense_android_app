package com.uav.defense.ui.components

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.Circle
import com.amap.api.maps2d.model.CircleOptions
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.MarkerOptions
import com.amap.api.maps2d.model.PolygonOptions
import com.uav.defense.data.models.PadTarget
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.AmberColor
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.PanelBg
import com.uav.defense.ui.theme.RadarGreen
import com.uav.defense.ui.theme.TextMain
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val RADAR_LAT = 39.909230
private const val RADAR_LNG = 116.397428
private const val RADAR_RANGE_M = 1800.0
private const val RADAR_AZIMUTH_CENTER = 48.0
private const val RADAR_SCAN_HALF = 60.0
private const val METERS_PER_LAT_DEG = 111000.0
private val METERS_PER_LNG_DEG = METERS_PER_LAT_DEG * cos(Math.toRadians(RADAR_LAT))
private const val TARGET_CIRCLE_RADIUS_METERS = 20.0
private const val TAP_DETECTION_RADIUS_METERS = 80.0
private const val RADAR_SWEEP_ARC_HALF_WIDTH_DEG = 8.0
private const val RADAR_SWEEP_SEGMENTS = 18

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
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }
    var amap by remember { mutableStateOf<AMap?>(null) }
    val targetCircleMap = remember { mutableMapOf<String, Circle>() }
    var scanPolygon by remember { mutableStateOf<com.amap.api.maps2d.model.Polygon?>(null) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose { lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (amap == null) {
                    amap = view.map?.apply {
                        mapType = AMap.MAP_TYPE_SATELLITE
                        uiSettings.apply {
                            isZoomControlsEnabled = false
                            isMyLocationButtonEnabled = false
                            isScaleControlsEnabled = false
                            isCompassEnabled = false
                        }
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(RADAR_LAT, RADAR_LNG), 14f
                            )
                        )
                        addPolygon(buildRadarCoveragePolygon())
                        addMarker(
                            MarkerOptions()
                                .position(LatLng(RADAR_LAT, RADAR_LNG))
                                .title("近程雷达-01 · 1.8km")
                                .anchor(0.5f, 0.5f)
                        )
                    }
                }
            }
        )

        LaunchedEffect(amap, currentMapMode) {
            amap?.mapType = when (currentMapMode) {
                "标准图" -> AMap.MAP_TYPE_NORMAL
                else -> AMap.MAP_TYPE_SATELLITE
            }
        }

        LaunchedEffect(amap, radarSweepAngle) {
            val map = amap ?: return@LaunchedEffect
            scanPolygon?.remove()
            scanPolygon = map.addPolygon(buildRadarSweepPolygon(radarSweepAngle.toDouble()))
        }

        LaunchedEffect(amap, targets, enabledTargetIds, selectedTargetId) {
            val map = amap ?: return@LaunchedEffect
            val enabledIds = targets.filter { it.id in enabledTargetIds }.map { it.id }.toSet()
            targetCircleMap.keys.toList().filter { it !in enabledIds }.forEach { id ->
                targetCircleMap[id]?.remove()
                targetCircleMap.remove(id)
            }
            targets.filter { it.id in enabledTargetIds }.forEach { t ->
                val latLng = LatLng(t.lat, t.lng)
                val fillColor = when (t.relation) {
                    "hostile" -> android.graphics.Color.argb(240, 255, 80, 80)
                    "friendly" -> android.graphics.Color.argb(240, 0, 255, 136)
                    else -> android.graphics.Color.argb(240, 255, 178, 69)
                }
                val strokeColor = if (selectedTargetId == t.id) {
                    android.graphics.Color.argb(255, 0, 245, 255)
                } else {
                    android.graphics.Color.argb(220, 255, 255, 255)
                }
                val existing = targetCircleMap[t.id]
                if (existing == null) {
                    val circle = map.addCircle(
                        CircleOptions()
                            .center(latLng)
                            .radius(TARGET_CIRCLE_RADIUS_METERS)
                            .fillColor(fillColor)
                            .strokeColor(strokeColor)
                            .strokeWidth(if (selectedTargetId == t.id) 5f else 3f)
                    )
                    if (circle != null) targetCircleMap[t.id] = circle
                } else {
                    existing.center = latLng
                    existing.fillColor = fillColor
                    existing.strokeColor = strokeColor
                    existing.strokeWidth = if (selectedTargetId == t.id) 5f else 3f
                }
            }
            map.setOnMapClickListener { tapped ->
                val targetId = findTappedTargetId(tapped, targets, enabledTargetIds)
                onTargetClick(targetId ?: "")
            }
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(PanelBg.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(currentMapMode, color = AccentCyan, fontSize = 10.sp)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(PanelBg.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onSwitchMapMode, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Layers, null, tint = AccentCyan, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onToggleMeasure, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Straighten, null, tint = if (measureMode) AccentCyan else TextMain, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = {
                amap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(RADAR_LAT, RADAR_LNG)))
            }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.GpsFixed, null, tint = TextMain, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = {
                amap?.moveCamera(CameraUpdateFactory.zoomIn())
            }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ZoomIn, null, tint = TextMain, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = {
                amap?.moveCamera(CameraUpdateFactory.zoomOut())
            }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ZoomOut, null, tint = TextMain, modifier = Modifier.size(22.dp))
            }
        }

        targets.find { it.id == selectedTargetId }?.let { target ->
            TargetPopup(
                target,
                onDismiss = { onTargetClick("") },
                onMarkFalsePositive = onMarkFalsePositive,
                onSendCommand = onSendCommand
            )
        }
    }
}

private fun buildRadarCoveragePolygon(): PolygonOptions {
    val points = mutableListOf<LatLng>()
    points.add(LatLng(RADAR_LAT, RADAR_LNG))
    val startAngle = RADAR_AZIMUTH_CENTER - RADAR_SCAN_HALF
    val endAngle = RADAR_AZIMUTH_CENTER + RADAR_SCAN_HALF
    for (i in 0..30) {
        val angleDeg = startAngle + (endAngle - startAngle) * i / 30.0
        val angleRad = Math.toRadians(angleDeg)
        val dLat = (RADAR_RANGE_M * cos(angleRad)) / METERS_PER_LAT_DEG
        val dLng = (RADAR_RANGE_M * sin(angleRad)) / METERS_PER_LNG_DEG
        points.add(LatLng(RADAR_LAT + dLat, RADAR_LNG + dLng))
    }
    points.add(LatLng(RADAR_LAT, RADAR_LNG))
    return PolygonOptions()
        .addAll(points)
        .fillColor(android.graphics.Color.argb(55, 0, 200, 100))
        .strokeColor(android.graphics.Color.argb(160, 0, 230, 120))
        .strokeWidth(2f)
}

/**
 * Builds the current radar sweep sector polygon using a heading angle in degrees
 * (clockwise from north), anchored at the radar center.
 */
private fun buildRadarSweepPolygon(sweepAngle: Double): PolygonOptions {
    val points = mutableListOf<LatLng>()
    points.add(LatLng(RADAR_LAT, RADAR_LNG))
    val startAngle = sweepAngle - RADAR_SWEEP_ARC_HALF_WIDTH_DEG
    val endAngle = sweepAngle + RADAR_SWEEP_ARC_HALF_WIDTH_DEG
    for (i in 0..RADAR_SWEEP_SEGMENTS) {
        val angleDeg = startAngle + (endAngle - startAngle) * i / RADAR_SWEEP_SEGMENTS
        val angleRad = Math.toRadians(angleDeg)
        val dLat = (RADAR_RANGE_M * cos(angleRad)) / METERS_PER_LAT_DEG
        val dLng = (RADAR_RANGE_M * sin(angleRad)) / METERS_PER_LNG_DEG
        points.add(LatLng(RADAR_LAT + dLat, RADAR_LNG + dLng))
    }
    points.add(LatLng(RADAR_LAT, RADAR_LNG))
    return PolygonOptions()
        .addAll(points)
        .fillColor(android.graphics.Color.argb(80, 0, 255, 140))
        .strokeColor(android.graphics.Color.argb(200, 80, 255, 180))
        .strokeWidth(1.5f)
}

private fun findTappedTargetId(
    tapped: LatLng,
    targets: List<PadTarget>,
    enabledTargetIds: Set<String>
): String? {
    return targets
        .asSequence()
        .filter { it.id in enabledTargetIds }
        .map { it.id to approximateDistanceMeters(tapped.latitude, tapped.longitude, it.lat, it.lng) }
        .filter { it.second <= TAP_DETECTION_RADIUS_METERS }
        .minByOrNull { it.second }
        ?.first
}

private fun approximateDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dx = (lng2 - lng1) * METERS_PER_LNG_DEG
    val dy = (lat2 - lat1) * METERS_PER_LAT_DEG
    return sqrt(dx * dx + dy * dy)
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
