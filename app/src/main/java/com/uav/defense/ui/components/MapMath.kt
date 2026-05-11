package com.uav.defense.ui.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun targetToXY(bearing: Float, distance: Float, centerX: Float, centerY: Float, scale: Float): Offset {
    val rad = Math.toRadians(bearing.toDouble())
    val px = (distance * scale * sin(rad)).toFloat()
    val py = -(distance * scale * cos(rad)).toFloat()
    return Offset(centerX + px, centerY + py)
}

fun distanceKm(p1: Offset, p2: Offset, scale: Float): Double {
    val dx = (p2.x - p1.x) / scale
    val dy = (p2.y - p1.y) / scale
    return sqrt(dx * dx + dy * dy).toDouble()
}
