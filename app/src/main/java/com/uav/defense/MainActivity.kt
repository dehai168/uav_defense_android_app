package com.uav.defense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.amap.api.maps.AMapSdk
import com.uav.defense.ui.screens.MainScreen
import com.uav.defense.ui.theme.UavDefenseTheme
import com.uav.defense.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Required by AMap SDK v8.1+ to enable map rendering
        AMapSdk.updatePrivacySetting(this, true, true)
        setContent {
            UavDefenseTheme {
                MainScreen(viewModel)
            }
        }
    }
}
