package com.uav.defense.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
import com.amap.api.maps2d.model.BitmapDescriptorFactory
import com.amap.api.maps2d.model.Circle
import com.amap.api.maps2d.model.CircleOptions
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.Marker
import com.amap.api.maps2d.model.MarkerOptions
import com.amap.api.maps2d.model.Polygon
import com.amap.api.maps2d.model.PolygonOptions
import com.amap.api.maps2d.model.Polyline
import com.amap.api.maps2d.model.PolylineOptions
import com.uav.defense.data.models.PadTarget
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.AmberColor
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.PanelBg
import com.uav.defense.ui.theme.RadarGreen
import com.uav.defense.ui.theme.TextMain
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val RADAR_LAT = 22.5307369
private const val RADAR_LNG = 114.0573761
private const val RADAR_RANGE_M = 1800.0
private const val METERS_PER_LAT_DEG = 111000.0
private val METERS_PER_LNG_DEG: Double = METERS_PER_LAT_DEG * cos(Math.toRadians(RADAR_LAT))
private const val TARGET_CIRCLE_RADIUS_METERS = 38.0
private const val TAP_DETECTION_RADIUS_METERS = 96.0
private const val DEFAULT_MAP_ZOOM = 15f
private const val MAP_SWEEP_TRAIL_TOTAL_ANGLE = 48.0
private const val MAP_SWEEP_TRAIL_SLICES = 40
private const val MAP_SWEEP_TRAIL_SLICE_OVERLAP = 0.35
private const val MAP_SWEEP_HEAD_STROKE_WIDTH = 5.5f
private const val TARGET_ALERT_RATIO = 0.5f
private const val TARGET_LABEL_TEXT_SIZE_DP = 11f
private const val TARGET_LABEL_HORIZONTAL_PADDING_DP = 8f
private const val TARGET_LABEL_VERTICAL_PADDING_DP = 4f
private const val TARGET_LABEL_INDICATOR_RADIUS_DP = 4f
private const val TARGET_LABEL_GAP_DP = 6f
private const val TARGET_LABEL_MIN_WIDTH_DP = 72f
private const val TARGET_LABEL_MIN_HEIGHT_DP = 26f
private val MAP_SWEEP_SLICE_ANGLE = MAP_SWEEP_TRAIL_TOTAL_ANGLE / MAP_SWEEP_TRAIL_SLICES.toDouble()
private val MAP_SWEEP_SLICE_SPECS = List(MAP_SWEEP_TRAIL_SLICES) { index ->
    val progress = (index + 1).toFloat() / MAP_SWEEP_TRAIL_SLICES
    MapSweepSliceSpec(
        angleOffset = index * MAP_SWEEP_SLICE_ANGLE,
        fillAlpha = (10 + progress * 105f).roundToInt(),
        strokeAlpha = (6 + progress * 24f).roundToInt()
    )
}

private data class MapSweepSliceSpec(
    val angleOffset: Double,
    val fillAlpha: Int,
    val strokeAlpha: Int
)

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
    val radarMarkerBitmap = remember(context) { createRadarMarkerBitmap(context.resources.displayMetrics.density) }
    val density = context.resources.displayMetrics.density
    var amap by remember { mutableStateOf<AMap?>(null) }
    val targetCircleMap = remember { mutableStateMapOf<String, Circle>() }
    val targetLabelMap = remember { mutableStateMapOf<String, Marker>() }
    val markerTargetMap = remember { mutableStateMapOf<String, String>() }
    val targetMarkerIdMap = remember { mutableStateMapOf<String, String>() }
    val sweepTrailSlices = remember { mutableStateListOf<Polygon>() }
    var sweepHeadLine by remember { mutableStateOf<Polyline?>(null) }
    var radarMarker by remember { mutableStateOf<Marker?>(null) }
    var suppressNextMapClick by remember { mutableStateOf(false) }

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
                        moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(RADAR_LAT, RADAR_LNG), DEFAULT_MAP_ZOOM))
                        radarMarker = addMarker(
                            MarkerOptions()
                                .position(LatLng(RADAR_LAT, RADAR_LNG))
                                .title("近程雷达-01 · 1.8km")
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromBitmap(radarMarkerBitmap))
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
            sweepTrailSlices.forEach { it.remove() }
            sweepTrailSlices.clear()
            sweepHeadLine?.remove()

            for (sliceSpec in MAP_SWEEP_SLICE_SPECS) {
                val startAngle = radarSweepAngle.toDouble() - MAP_SWEEP_TRAIL_TOTAL_ANGLE + sliceSpec.angleOffset
                val endAngle = startAngle + MAP_SWEEP_SLICE_ANGLE + MAP_SWEEP_TRAIL_SLICE_OVERLAP
                map.addPolygon(
                    buildRadarSweepSlice(
                        startAngle = startAngle,
                        endAngle = endAngle,
                        distanceMeters = RADAR_RANGE_M,
                        fillAlpha = sliceSpec.fillAlpha,
                        strokeAlpha = sliceSpec.strokeAlpha
                    )
                )?.let(sweepTrailSlices::add)
            }
            sweepHeadLine = map.addPolyline(
                buildRadarSweepLine(
                    sweepAngle = radarSweepAngle.toDouble(),
                    distanceMeters = RADAR_RANGE_M,
                    strokeAlpha = 235,
                    strokeWidth = MAP_SWEEP_HEAD_STROKE_WIDTH
                )
            )
            sweepHeadLine?.zIndex = 19f
            radarMarker?.zIndex = 20f
        }

        LaunchedEffect(amap, targets, enabledTargetIds, selectedTargetId) {
            val map = amap ?: return@LaunchedEffect
            val enabledIds = targets.filter { it.id in enabledTargetIds }.map { it.id }.toSet()
            targetCircleMap.keys.toList().filter { it !in enabledIds }.forEach { id ->
                targetCircleMap[id]?.remove()
                targetCircleMap.remove(id)
            }
            targetLabelMap.keys.toList().filter { it !in enabledIds }.forEach { id ->
                targetLabelMap[id]?.remove()
                targetMarkerIdMap.remove(id)?.let(markerTargetMap::remove)
                targetLabelMap.remove(id)
            }
            targets.filter { it.id in enabledTargetIds }.forEach { target ->
                val latLng = LatLng(target.lat, target.lng)
                val baseColor = targetDistanceColor(target.distance)
                val strokeColor = if (selectedTargetId == target.id) android.graphics.Color.argb(255, 0, 212, 255) else baseColor
                val existing = targetCircleMap[target.id]
                if (existing == null) {
                    map.addCircle(
                        CircleOptions()
                            .center(latLng)
                            .radius(TARGET_CIRCLE_RADIUS_METERS)
                            .fillColor(baseColor)
                            .strokeColor(strokeColor)
                            .strokeWidth(if (selectedTargetId == target.id) 6f else 3f)
                    )?.let { targetCircleMap[target.id] = it }
                } else {
                    existing.center = latLng
                    existing.fillColor = baseColor
                    existing.strokeColor = strokeColor
                    existing.strokeWidth = if (selectedTargetId == target.id) 6f else 3f
                }

                val labelMarker = targetLabelMap[target.id]
                val labelBitmap = createTargetLabelBitmap(
                    density = density,
                    title = target.droneModel,
                    color = strokeColor,
                    emphasized = selectedTargetId == target.id
                )
                if (labelMarker == null) {
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(target.droneModel)
                            .anchor(0f, 1f)
                            .icon(BitmapDescriptorFactory.fromBitmap(labelBitmap))
                    )?.apply {
                        zIndex = 18f
                    }?.let {
                        targetLabelMap[target.id] = it
                        markerTargetMap[it.id] = target.id
                        targetMarkerIdMap[target.id] = it.id
                    }
                } else {
                    labelMarker.position = latLng
                    labelMarker.title = target.droneModel
                    labelMarker.setIcon(BitmapDescriptorFactory.fromBitmap(labelBitmap))
                    labelMarker.zIndex = 18f
                    markerTargetMap[labelMarker.id] = target.id
                    targetMarkerIdMap[target.id] = labelMarker.id
                }
            }
            map.setOnMapClickListener { tapped ->
                if (suppressNextMapClick) {
                    suppressNextMapClick = false
                    return@setOnMapClickListener
                }
                val targetId = findTappedTargetId(tapped, targets, enabledTargetIds)
                onTargetClick(targetId ?: "")
            }
            map.setOnMarkerClickListener { marker ->
                markerTargetMap[marker.id]?.let {
                    suppressNextMapClick = true
                    onTargetClick(it)
                    true
                } ?: false
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
            IconButton(onClick = { amap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(RADAR_LAT, RADAR_LNG), DEFAULT_MAP_ZOOM)) }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.GpsFixed, null, tint = TextMain, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { amap?.moveCamera(CameraUpdateFactory.zoomIn()) }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ZoomIn, null, tint = TextMain, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { amap?.moveCamera(CameraUpdateFactory.zoomOut()) }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.ZoomOut, null, tint = TextMain, modifier = Modifier.size(22.dp))
            }
        }

        targets.find { it.id == selectedTargetId }?.let { target ->
            TargetPopup(
                target = target,
                onDismiss = { onTargetClick("") },
                onMarkFalsePositive = onMarkFalsePositive,
                onSendCommand = onSendCommand
            )
        }
    }
}

private fun pointAt(angleDeg: Double, distanceMeters: Double): LatLng {
    val angleRad = Math.toRadians(angleDeg)
    val dLat = (distanceMeters * cos(angleRad)) / METERS_PER_LAT_DEG
    val dLng = (distanceMeters * sin(angleRad)) / METERS_PER_LNG_DEG
    return LatLng(RADAR_LAT + dLat, RADAR_LNG + dLng)
}

private fun buildRadarSweepLine(
    sweepAngle: Double,
    distanceMeters: Double,
    strokeAlpha: Int,
    strokeWidth: Float
): PolylineOptions {
    return PolylineOptions()
        .add(LatLng(RADAR_LAT, RADAR_LNG), pointAt(sweepAngle, distanceMeters))
        .color(android.graphics.Color.argb(strokeAlpha.coerceIn(0, 255), 80, 255, 180))
        .width(strokeWidth)
}

private fun buildRadarSweepSlice(
    startAngle: Double,
    endAngle: Double,
    distanceMeters: Double,
    fillAlpha: Int,
    strokeAlpha: Int
): PolygonOptions {
    return PolygonOptions()
        .add(
            LatLng(RADAR_LAT, RADAR_LNG),
            pointAt(startAngle, distanceMeters),
            pointAt(endAngle, distanceMeters)
        )
        .fillColor(android.graphics.Color.argb(fillAlpha.coerceIn(0, 255), 80, 255, 180))
        .strokeColor(android.graphics.Color.argb(strokeAlpha.coerceIn(0, 255), 80, 255, 180))
        .strokeWidth(1f)
}

private fun targetDistanceColor(distanceKm: Float): Int {
    val thresholdKm = RADAR_RANGE_M.toFloat() / 1000f * TARGET_ALERT_RATIO
    return if (distanceKm <= thresholdKm) {
        android.graphics.Color.argb(248, 255, 59, 48)
    } else {
        android.graphics.Color.argb(248, 0, 255, 136)
    }
}

private fun createRadarMarkerBitmap(density: Float): Bitmap {
    val size = (40f * density).roundToInt().coerceAtLeast(48)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val center = size / 2f
    val radius = size * 0.26f

    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(255, 0, 212, 255)
        style = Paint.Style.STROKE
        strokeWidth = size * 0.07f
    }
    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(255, 0, 255, 136)
        style = Paint.Style.FILL
    }
    val wingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(235, 0, 160, 255)
        style = Paint.Style.FILL
    }

    canvas.drawCircle(center, center, radius * 1.4f, ringPaint)

    val bodyPath = Path().apply {
        moveTo(center, center - radius * 1.55f)
        lineTo(center + radius * 0.45f, center)
        lineTo(center, center + radius * 1.2f)
        lineTo(center - radius * 0.45f, center)
        close()
    }
    val wingPath = Path().apply {
        moveTo(center - radius * 1.7f, center + radius * 0.1f)
        lineTo(center - radius * 0.15f, center - radius * 0.15f)
        lineTo(center - radius * 0.15f, center + radius * 0.6f)
        close()
        moveTo(center + radius * 1.7f, center + radius * 0.1f)
        lineTo(center + radius * 0.15f, center - radius * 0.15f)
        lineTo(center + radius * 0.15f, center + radius * 0.6f)
        close()
    }

    canvas.drawPath(wingPath, wingPaint)
    canvas.drawPath(bodyPath, corePaint)
    canvas.drawCircle(center, center, radius * 0.3f, ringPaint)
    return bitmap
}

private fun createTargetLabelBitmap(
    density: Float,
    title: String,
    color: Int,
    emphasized: Boolean
): Bitmap {
    val textSize = TARGET_LABEL_TEXT_SIZE_DP * density
    val horizontalPadding = TARGET_LABEL_HORIZONTAL_PADDING_DP * density
    val verticalPadding = TARGET_LABEL_VERTICAL_PADDING_DP * density
    val indicatorRadius = TARGET_LABEL_INDICATOR_RADIUS_DP * density
    val gap = TARGET_LABEL_GAP_DP * density

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        this.textSize = textSize
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, if (emphasized) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }
    val textWidth = textPaint.measureText(title)
    val textHeight = (textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent).roundToInt()
    val width = (horizontalPadding * 2 + indicatorRadius * 2 + gap + textWidth).roundToInt()
        .coerceAtLeast((TARGET_LABEL_MIN_WIDTH_DP * density).roundToInt())
    val height = (verticalPadding * 2 + textHeight).roundToInt()
        .coerceAtLeast((TARGET_LABEL_MIN_HEIGHT_DP * density).roundToInt())
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(if (emphasized) 238 else 212, 7, 18, 28)
        style = Paint.Style.FILL
    }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = if (emphasized) android.graphics.Color.argb(255, 0, 212, 255) else android.graphics.Color.argb(255, 64, 255, 192)
        style = Paint.Style.STROKE
        strokeWidth = density * 1.2f
    }
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    val radius = 8f * density
    canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
    canvas.drawRoundRect(rect, radius, radius, borderPaint)

    val centerY = height / 2f
    canvas.drawCircle(horizontalPadding + indicatorRadius, centerY, indicatorRadius, dotPaint)
    val baseline = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(title, horizontalPadding + indicatorRadius * 2 + gap, baseline, textPaint)
    return bitmap
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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMain, modifier = Modifier.size(16.dp))
                    }
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
                lines.forEach { (label, value) -> Text("$label：$value", color = TextMain, fontSize = 11.sp) }
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
    ) {
        Text(label, fontSize = 10.sp)
    }
}
