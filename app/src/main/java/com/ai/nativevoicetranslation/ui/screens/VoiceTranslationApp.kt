package com.ai.nativevoicetranslation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.nativevoicetranslation.model.LanguageOption
import com.ai.nativevoicetranslation.model.PipelineStage
import com.ai.nativevoicetranslation.model.VoiceTranslationUiState
import com.ai.nativevoicetranslation.viewmodel.VoiceTranslationViewModel

@Composable
fun VoiceTranslationApp(
    viewModel: VoiceTranslationViewModel,
    hasRecordPermission: Boolean,
    onRequestRecordPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderSection(uiState = uiState)
            }
            item {
                LanguageSection(
                    uiState = uiState,
                    supportedLanguages = viewModel.supportedLanguages,
                    onSourceChanged = viewModel::updateSourceLanguage,
                    onTargetChanged = viewModel::updateTargetLanguage,
                    onSwapLanguages = viewModel::swapLanguages
                )
            }
            item {
                RecordSection(
                    uiState = uiState.copy(isPermissionGranted = hasRecordPermission),
                    onPress = {
                        if (!hasRecordPermission) {
                            onRequestRecordPermission()
                        } else {
                            viewModel.onPermissionResult(true)
                            viewModel.startRecording()
                        }
                    },
                    onRelease = viewModel::stopRecordingAndProcess,
                    onReset = viewModel::resetConversation
                )
            }
            item {
                ResultSection(uiState = uiState)
            }
            item {
                ArchitectureSection(uiState = uiState)
            }
        }
    }
}

@Composable
private fun HeaderSection(uiState: VoiceTranslationUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AI Native Voice Translation App", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("本地离线语音互翻译 Android App MVP 框架", style = MaterialTheme.typography.bodyLarge)
            Text(uiState.statusMessage, style = MaterialTheme.typography.bodyMedium)
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RecordSection(
    uiState: VoiceTranslationUiState,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onReset: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("交互流程", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("按住说话 → 松开 → ASR → MT → TTS → 自动播放", style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.stage == PipelineStage.Recording) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    .pointerInput(uiState.stage, uiState.isPermissionGranted) {
                        detectTapGestures(onPress = {
                            onPress()
                            tryAwaitRelease()
                            onRelease()
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (uiState.stage == PipelineStage.Recording) "松开结束" else "按住说话",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StageChip(uiState.stage)
                AssistChip(onClick = onReset, label = { Text("清空结果") })
            }
            if (!uiState.isPermissionGranted) {
                OutlinedButton(onClick = onPress) {
                    Text("请求麦克风权限")
                }
            }
        }
    }
}

@Composable
private fun ResultSection(uiState: VoiceTranslationUiState) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("翻译结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ResultBlock(title = "ASR 文本", value = uiState.sourceText.ifBlank { "等待语音输入..." })
            ResultBlock(title = "MT 文本", value = uiState.translatedText.ifBlank { "等待翻译输出..." })
        }
    }
}

@Composable
private fun ResultBlock(title: String, value: String) {
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ArchitectureSection(uiState: VoiceTranslationUiState) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("技术框架", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(uiState.nativeRuntimeSummary, style = MaterialTheme.typography.bodyMedium)
            uiState.architectureNotes.forEach { note ->
                Text("• $note", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSection(
    uiState: VoiceTranslationUiState,
    supportedLanguages: List<LanguageOption>,
    onSourceChanged: (LanguageOption) -> Unit,
    onTargetChanged: (LanguageOption) -> Unit,
    onSwapLanguages: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("语言预设", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LanguageDropdown(
                label = "本地语言",
                selected = uiState.sourceLanguage,
                options = supportedLanguages,
                onSelected = onSourceChanged
            )
            LanguageDropdown(
                label = "目标语言",
                selected = uiState.targetLanguage,
                options = supportedLanguages,
                onSelected = onTargetChanged
            )
            Button(onClick = onSwapLanguages, modifier = Modifier.fillMaxWidth()) {
                Text("交换语言方向")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    label: String,
    selected: LanguageOption,
    options: List<LanguageOption>,
    onSelected: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StageChip(stage: PipelineStage) {
    AssistChip(onClick = {}, label = { Text("状态：${stage.name}") })
}
