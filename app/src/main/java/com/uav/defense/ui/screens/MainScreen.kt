package com.uav.defense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uav.defense.data.MockData
import com.uav.defense.ui.components.LeftMenu
import com.uav.defense.ui.components.MapPanel
import com.uav.defense.ui.components.RadarPanel
import com.uav.defense.ui.components.ToastOverlay
import com.uav.defense.ui.components.VideoPanel
import com.uav.defense.ui.dialogs.AnalysisDialog
import com.uav.defense.ui.dialogs.ManageDialog
import com.uav.defense.ui.dialogs.PlaybackDialog
import com.uav.defense.ui.dialogs.ScanDialog
import com.uav.defense.ui.dialogs.SettingsDialog
import com.uav.defense.ui.theme.BackgroundDark
import com.uav.defense.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val targets by viewModel.targets.collectAsStateWithLifecycle()
    val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
    val activeFullscreenPanel by viewModel.activeFullscreenPanel.collectAsStateWithLifecycle()
    val measureMode by viewModel.measureMode.collectAsStateWithLifecycle()
    val relationFilter by viewModel.relationFilter.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val selectedTargetId by viewModel.selectedTargetId.collectAsStateWithLifecycle()
    val currentMapMode by viewModel.currentMapMode.collectAsStateWithLifecycle()
    val radarSweepAngle by viewModel.radarSweepAngle.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val toasts by viewModel.toasts.collectAsStateWithLifecycle()
    val radarParams by viewModel.radarParams.collectAsStateWithLifecycle()
    val enabledTargetIds by viewModel.enabledTargetIds.collectAsStateWithLifecycle()

    val hostileTargets = targets.filter { it.relation == "hostile" }
    val activeTarget = selectedTargetId?.let { id -> targets.find { it.id == id } }

    Box(Modifier.fillMaxSize().background(BackgroundDark)) {
        when (activeFullscreenPanel) {
            "map" -> MapPanel(
                targets, enabledTargetIds, selectedTargetId, radarSweepAngle, currentMapMode, measureMode,
                onTargetClick = { viewModel.selectTarget(it.ifEmpty { null }) },
                onSwitchMapMode = viewModel::switchMapMode,
                onToggleMeasure = viewModel::toggleMeasureMode,
                onMarkFalsePositive = viewModel::markFalsePositive,
                onSendCommand = viewModel::sendCommand,
                onDoubleTap = { viewModel.setFullscreen(null) },
                modifier = Modifier.fillMaxSize()
            )

            "radar" -> RadarPanel(targets, enabledTargetIds, radarSweepAngle, currentTime, selectedTargetId,
                onTargetClick = { viewModel.selectTarget(it) }, onDoubleTap = { viewModel.setFullscreen(null) }, modifier = Modifier.fillMaxSize())

            "video" -> VideoPanel(activeTarget, hostileTargets, MockData.cameras, currentTime,
                onDoubleTap = { viewModel.setFullscreen(null) }, modifier = Modifier.fillMaxSize())

            else -> Row(Modifier.fillMaxSize()) {
                MapPanel(
                    targets, enabledTargetIds, selectedTargetId, radarSweepAngle, currentMapMode, measureMode,
                    onTargetClick = { viewModel.selectTarget(it.ifEmpty { null }) },
                    onSwitchMapMode = viewModel::switchMapMode,
                    onToggleMeasure = viewModel::toggleMeasureMode,
                    onMarkFalsePositive = viewModel::markFalsePositive,
                    onSendCommand = viewModel::sendCommand,
                    onDoubleTap = { viewModel.setFullscreen("map") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                Spacer(Modifier.width(4.dp))

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RadarPanel(targets, enabledTargetIds, radarSweepAngle, currentTime, selectedTargetId,
                        onTargetClick = { viewModel.selectTarget(it) },
                        onDoubleTap = { viewModel.setFullscreen("radar") },
                        modifier = Modifier.weight(1f).fillMaxWidth())

                    VideoPanel(activeTarget, hostileTargets, MockData.cameras, currentTime,
                        onDoubleTap = { viewModel.setFullscreen("video") },
                        modifier = Modifier.weight(1f).fillMaxWidth())
                }
            }
        }

        // LeftMenu floats on top of all panels in normal view
        if (activeFullscreenPanel == null) {
            LeftMenu(
                activeModal = activeModal,
                modifier = Modifier.align(Alignment.CenterStart),
                onMenuClick = { viewModel.toggleModal(it) }
            )
        }

        when (activeModal) {
            "scan" -> ScanDialog(targets, enabledTargetIds, relationFilter, selectedCategories,
                onClose = viewModel::closeModal,
                onSetRelationFilter = viewModel::setRelationFilter,
                onToggleCategory = viewModel::toggleCategory,
                onSelectTarget = viewModel::selectTarget,
                onToggleEnabled = viewModel::toggleTargetEnabled)

            "manage" -> ManageDialog(radarParams, onClose = viewModel::closeModal, onSave = viewModel::saveRadarParams)
            "playback" -> PlaybackDialog(targets, onClose = viewModel::closeModal)
            "analysis" -> AnalysisDialog(targets, onClose = viewModel::closeModal)
            "settings" -> SettingsDialog(onClose = viewModel::closeModal, onToast = { viewModel.addToast(it) })
        }

        ToastOverlay(toasts)
    }
}
