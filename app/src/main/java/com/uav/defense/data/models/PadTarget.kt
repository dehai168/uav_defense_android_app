package com.uav.defense.data.models

data class PadTarget(
    val id: String,
    val level: String,
    val droneModel: String,
    val frequency: String,
    val bearing: Float,
    val distance: Float,
    val altitude: Float,
    val trajectoryPrediction: String,
    val reviewStatus: String,
    val detectedAt: String,
    val lng: Double,
    val lat: Double,
    val speed: Float,
    val typeLabel: String,
    val relation: String,
    val eta: String,
    val pitch: Float,
    val verticalSpeed: Float,
    val enabled: Boolean = true,
    val actionStatus: String = ""
)
