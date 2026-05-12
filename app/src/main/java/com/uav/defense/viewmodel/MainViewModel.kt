package com.uav.defense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uav.defense.data.MockData
import com.uav.defense.data.models.PadTarget
import com.uav.defense.data.models.RadarParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ToastData(val id: String, val message: String, val type: String)

class MainViewModel : ViewModel() {
    companion object {
        private const val FULL_CIRCLE_DEGREES = 360f
        private const val MAX_BEARING_DELTA = 2f
        private const val MAX_DISTANCE_DELTA = 0.015f
        private const val MIN_DISTANCE_KM = 0.3f
        private const val MAX_DISTANCE_KM = 1.5f
        private const val RADAR_LAT = 39.909230
        private const val RADAR_LNG = 116.397428
        private const val METERS_PER_LAT_DEG = 111000.0
        // Longitude-degree meter length depends on latitude; computed once for radar latitude.
        private val METERS_PER_LNG_DEG: Double = METERS_PER_LAT_DEG * cos(Math.toRadians(RADAR_LAT))
    }
    private val _targets = MutableStateFlow(MockData.targets)
    val targets: StateFlow<List<PadTarget>> = _targets.asStateFlow()

    private val _activeModal = MutableStateFlow<String?>(null)
    val activeModal = _activeModal.asStateFlow()

    private val _activeFullscreenPanel = MutableStateFlow<String?>(null)
    val activeFullscreenPanel = _activeFullscreenPanel.asStateFlow()

    private val _measureMode = MutableStateFlow(false)
    val measureMode = _measureMode.asStateFlow()

    private val _relationFilter = MutableStateFlow("all")
    val relationFilter = _relationFilter.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()

    private val _selectedTargetId = MutableStateFlow<String?>(null)
    val selectedTargetId = _selectedTargetId.asStateFlow()

    private val _currentMapMode = MutableStateFlow("卫星图")
    val currentMapMode = _currentMapMode.asStateFlow()

    private val _radarSweepAngle = MutableStateFlow(0f)
    val radarSweepAngle = _radarSweepAngle.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime = _currentTime.asStateFlow()

    private val _toasts = MutableStateFlow<List<ToastData>>(emptyList())
    val toasts = _toasts.asStateFlow()

    private val _radarParams = MutableStateFlow(RadarParams())
    val radarParams = _radarParams.asStateFlow()

    private val _enabledTargetIds = MutableStateFlow(MockData.targets.map { it.id }.toSet())
    val enabledTargetIds = _enabledTargetIds.asStateFlow()

    init {
        updateCurrentTime()
        viewModelScope.launch {
            while (true) {
                delay(50)
                updateRadarSweep()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateCurrentTime()
                updateTargetPositions()
            }
        }
    }

    fun toggleModal(modal: String) { _activeModal.update { if (it == modal) null else modal } }
    fun closeModal() { _activeModal.value = null }
    fun setFullscreen(panel: String?) { _activeFullscreenPanel.value = panel }
    fun selectTarget(id: String?) { _selectedTargetId.value = id }
    fun toggleTargetEnabled(id: String) { _enabledTargetIds.update { if (id in it) it - id else it + id } }
    fun setRelationFilter(f: String) { _relationFilter.value = f }
    fun toggleCategory(cat: String) { _selectedCategories.update { if (cat in it) it - cat else it + cat } }
    fun toggleMeasureMode() { _measureMode.update { !it } }

    fun switchMapMode() {
        _currentMapMode.update {
            when (it) {
                "卫星图" -> "标准图"
                else -> "卫星图"
            }
        }
    }

    fun markFalsePositive(id: String) {
        _targets.update { list -> list.map { if (it.id == id) it.copy(reviewStatus = "false-positive", actionStatus = "已标记为误报") else it } }
        addToast("目标 $id 已标记为误报", "warning")
    }

    fun sendCommand(targetId: String, command: String) {
        _targets.update { list -> list.map { if (it.id == targetId) it.copy(actionStatus = "已发送指令: $command") else it } }
        addToast("已对目标 $targetId 发送「$command」指令", "success")
    }

    fun saveRadarParams(params: RadarParams) {
        _radarParams.value = params
        addToast("设备参数已保存到演示态配置", "success")
    }

    fun addToast(message: String, type: String = "info") {
        val id = UUID.randomUUID().toString()
        _toasts.update { it + ToastData(id, message, type) }
        viewModelScope.launch {
            delay(2600)
            _toasts.update { list -> list.filterNot { it.id == id } }
        }
    }

    fun updateTargetPositions() {
        _targets.update { list ->
            list.map {
                val bearingDelta = Random.nextFloat() * (MAX_BEARING_DELTA * 2f) - MAX_BEARING_DELTA
                val distanceDelta = Random.nextFloat() * (MAX_DISTANCE_DELTA * 2f) - MAX_DISTANCE_DELTA
                val bearing = (it.bearing + bearingDelta + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
                val distance = (it.distance + distanceDelta).coerceIn(MIN_DISTANCE_KM, MAX_DISTANCE_KM)
                val rad = Math.toRadians(bearing.toDouble())
                val distanceMeters = distance * 1000.0
                val dLat = (distanceMeters * cos(rad)) / METERS_PER_LAT_DEG
                val dLng = (distanceMeters * sin(rad)) / METERS_PER_LNG_DEG
                it.copy(
                    bearing = bearing,
                    distance = distance,
                    lat = RADAR_LAT + dLat,
                    lng = RADAR_LNG + dLng
                )
            }
        }
    }

    fun updateRadarSweep() { _radarSweepAngle.update { (it + 3f) % FULL_CIRCLE_DEGREES } }

    private fun updateCurrentTime() {
        _currentTime.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
