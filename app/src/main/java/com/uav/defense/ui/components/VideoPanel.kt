package com.uav.defense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.uav.defense.data.models.Camera
import com.uav.defense.data.models.PadTarget
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.TextMain
import kotlinx.coroutines.delay
import kotlin.math.sin

private const val VIDEO_ASSET_URI = "asset:///out.mp4"
private const val CROSSHAIR_ALPHA = 0.72f
private const val CROSSHAIR_STROKE_WIDTH = 1.5f
private val TRACKING_OVERLAY_COLOR = Color(0xFF00FF88)

@Composable
fun VideoPanel(
    activeTarget: PadTarget?,
    hostileTargets: List<PadTarget>,
    cameras: List<Camera>,
    currentTime: String,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recBlink by remember { mutableStateOf(true) }
    var tick by remember { mutableLongStateOf(0L) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            setMediaItem(MediaItem.fromUri(VIDEO_ASSET_URI))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) { onDispose { player.release() } }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            recBlink = !recBlink
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            tick += 50
        }
    }

    val pan = activeTarget?.bearing ?: hostileTargets.firstOrNull()?.bearing ?: 0f
    val tilt = activeTarget?.pitch ?: hostileTargets.firstOrNull()?.pitch ?: 0f
    val activeCamera = cameras.firstOrNull { it.trackingStatus == "tracking" || it.trackingStatus == "locked" }

    Box(modifier = modifier.background(Color(0xFF020810)).pointerInput(Unit) { detectTapGestures(onDoubleTap = { onDoubleTap() }) }) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    useController = false
                    this.player = player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val overlayColor = TRACKING_OVERLAY_COLOR.copy(alpha = CROSSHAIR_ALPHA)
            drawLine(overlayColor, Offset(0f, cy), Offset(size.width, cy), CROSSHAIR_STROKE_WIDTH)
            drawLine(overlayColor, Offset(cx, 0f), Offset(cx, size.height), CROSSHAIR_STROKE_WIDTH)

            val jitter = (sin(tick * 0.01) * 4).toFloat()
            val bx = cx - 40f + jitter
            val by = cy - 30f + jitter / 2f
            val cornerLength = 14f
            drawLine(overlayColor, Offset(bx, by), Offset(bx + cornerLength, by), 1.5f)
            drawLine(overlayColor, Offset(bx, by), Offset(bx, by + cornerLength), 1.5f)
            drawLine(overlayColor, Offset(bx + 80f, by), Offset(bx + 80f - cornerLength, by), 1.5f)
            drawLine(overlayColor, Offset(bx + 80f, by), Offset(bx + 80f, by + cornerLength), 1.5f)
            drawLine(overlayColor, Offset(bx, by + 60f), Offset(bx + cornerLength, by + 60f), 1.5f)
            drawLine(overlayColor, Offset(bx, by + 60f), Offset(bx, by + 60f - cornerLength), 1.5f)
            drawLine(overlayColor, Offset(bx + 80f, by + 60f), Offset(bx + 80f - cornerLength, by + 60f), 1.5f)
            drawLine(overlayColor, Offset(bx + 80f, by + 60f), Offset(bx + 80f, by + 60f - cornerLength), 1.5f)
        }

        Row(Modifier.align(Alignment.TopStart).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).background(if (recBlink) DangerRed else Color.Transparent, CircleShape))
            Text("REC  $currentTime", color = Color.White, fontSize = 12.sp)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(0.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(activeCamera?.deviceName ?: "光电转台-01", color = TextMain, fontSize = 11.sp)
            Text("转角 ${"%.1f".format(pan)}°", color = TextMain, fontSize = 11.sp)
            Text("仰角 ${"%.1f".format(tilt)}°", color = TextMain, fontSize = 11.sp)
            Text("码流 1080P / 25FPS", color = TextMain, fontSize = 11.sp)
        }
    }
}
