package com.uav.defense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.uav.defense.ui.screens.MainScreen
import com.uav.defense.ui.theme.UavDefenseTheme
import com.uav.defense.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UavDefenseTheme {
                MainScreen(viewModel)
            }
        }
    }
}
