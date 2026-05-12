package com.uav.defense.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uav.defense.data.models.RadarParams
import com.uav.defense.ui.theme.AccentCyan
import com.uav.defense.ui.theme.BorderColor
import com.uav.defense.ui.theme.TextMain

@Composable
fun ManageDialog(radarParams: RadarParams, onClose: () -> Unit, onSave: (RadarParams) -> Unit) {
    var name by remember { mutableStateOf(radarParams.name) }
    var lng by remember { mutableStateOf(radarParams.lng) }
    var lat by remember { mutableStateOf(radarParams.lat) }
    var altitude by remember { mutableStateOf(radarParams.altitude) }
    var azimuth by remember { mutableStateOf(radarParams.azimuth) }
    var pitch by remember { mutableStateOf(radarParams.pitch) }
    var roll by remember { mutableStateOf(radarParams.roll) }
    var azScan by remember { mutableStateOf(radarParams.azScan) }
    var elScan by remember { mutableStateOf(radarParams.elScan) }
    var radius by remember { mutableStateOf(radarParams.radius) }
    var clutterLevel by remember { mutableStateOf(radarParams.clutterLevel) }
    var cameraMode by remember { mutableStateOf(radarParams.cameraMode) }

    MenuDialogFrame(title = "设备管理", onClose = onClose) { bodyModifier ->
        androidx.compose.foundation.layout.Column(
            modifier = bodyModifier.verticalScroll(rememberScrollState())
        ) {
            val fields = listOf(
                Triple("雷达名称", name) { v: String -> name = v },
                Triple("经度", lng) { v: String -> lng = v },
                Triple("纬度", lat) { v: String -> lat = v },
                Triple("海拔", altitude) { v: String -> altitude = v },
                Triple("方位角", azimuth) { v: String -> azimuth = v },
                Triple("俯仰角", pitch) { v: String -> pitch = v },
                Triple("滚动角", roll) { v: String -> roll = v },
                Triple("方位扫描", azScan) { v: String -> azScan = v },
                Triple("俯仰扫描", elScan) { v: String -> elScan = v },
                Triple("扫描半径", radius) { v: String -> radius = v },
                Triple("杂波抑制", clutterLevel) { v: String -> clutterLevel = v },
                Triple("摄像机模式", cameraMode) { v: String -> cameraMode = v }
            )

            fields.forEach { (label, value, onUpdate) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        label,
                        color = TextMain.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        modifier = Modifier.width(90.dp)
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = onUpdate,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = AccentCyan
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    onSave(RadarParams(name, lng, lat, altitude, azimuth, pitch, roll, azScan, elScan, radius, clutterLevel, cameraMode))
                    onClose()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("保存参数", color = Color.Black)
            }
        }
    }
}
