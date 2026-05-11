package com.uav.defense.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.AmberColor
import com.uav.defense.ui.theme.DangerRed
import com.uav.defense.ui.theme.RadarGreen
import com.uav.defense.ui.theme.TextMain
import com.uav.defense.viewmodel.ToastData

@Composable
fun ToastOverlay(toasts: List<ToastData>) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            toasts.forEach { toast ->
                key(toast.id) {
                    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        val (bg, icon, tint) = when (toast.type) {
                            "success" -> Triple(Color(0xFF1A3A2A), Icons.Default.CheckCircle, RadarGreen)
                            "warning" -> Triple(Color(0xFF3A2A0A), Icons.Default.Warning, AmberColor)
                            "error" -> Triple(Color(0xFF3A0A0A), Icons.Default.Error, DangerRed)
                            else -> Triple(Color(0xFF0A1A3A), Icons.Default.Info, AccentCyan)
                        }
                        Row(
                            modifier = Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                            Text(toast.message, color = TextMain, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
