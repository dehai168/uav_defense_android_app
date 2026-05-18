package com.ai.nativevoicetranslation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ai.nativevoicetranslation.ui.screens.VoiceTranslationApp
import com.ai.nativevoicetranslation.ui.theme.AiNativeVoiceTranslationTheme
import com.ai.nativevoicetranslation.viewmodel.VoiceTranslationViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<VoiceTranslationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                )
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasPermission = granted
                viewModel.onPermissionResult(granted)
            }

            AiNativeVoiceTranslationTheme {
                VoiceTranslationApp(
                    viewModel = viewModel,
                    hasRecordPermission = hasPermission,
                    onRequestRecordPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        viewModel.shutdown()
        super.onDestroy()
    }
}
