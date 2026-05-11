package com.uav.defense.data.models

data class RadarParams(
    val name: String = "近程雷达-01",
    val lng: String = "116.397428",
    val lat: String = "39.909230",
    val altitude: String = "58",
    val azimuth: String = "48",
    val pitch: String = "12",
    val roll: String = "0",
    val azScan: String = "0~120°",
    val elScan: String = "0~60°",
    val radius: String = "1800",
    val clutterLevel: String = "3",
    val cameraMode: String = "VIS/IR双模"
)
