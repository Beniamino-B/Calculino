package com.calculino.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.data.prefs.SettingsRepository
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.settings.AppSettings
import com.calculino.app.domain.settings.ThemeMode
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape

@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    historyRepository: HistoryRepository,
    onOpenAi: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = viewModel(
        initializer = { SettingsViewModel(settingsRepository, historyRepository) },
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsScreen(
        settings = settings,
        onThemeModeChange = viewModel::setThemeMode,
        onHapticsChange = viewModel::setHapticsEnabled,
        onLocalAiChange = viewModel::setLocalAiEnabled,
        onCameraOcrChange = viewModel::setCameraOcrEnabled,
        onClearHistory = viewModel::clearHistory,
        onOpenAi = onOpenAi,
        onOpenAbout = onOpenAbout,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onLocalAiChange: (Boolean) -> Unit,
    onCameraOcrChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        KawaiiBackdrop()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = CalculinoSizes.screenMaxWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = CalculinoSpacing.lg,
                    vertical = CalculinoSpacing.sm,
                ),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            SettingsHeader()
            SettingsSection(
                title = stringResource(R.string.settings_section_appearance),
                icon = Icons.Rounded.Palette,
            ) {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeChoiceChip(
                            label = themeLabel(mode),
                            selected = settings.themeMode == mode,
                            onClick = {
                                if (settings.themeMode != mode) onThemeModeChange(mode)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                SettingSwitchRow(
                    title = stringResource(R.string.settings_haptics),
                    subtitle = stringResource(R.string.settings_haptics_description),
                    checked = settings.hapticsEnabled,
                    enabled = true,
                    onCheckedChange = onHapticsChange,
                )
            }
            SettingsSection(
                title = stringResource(R.string.settings_section_data),
                icon = Icons.Rounded.DeleteSweep,
            ) {
                Text(
                    text = stringResource(R.string.settings_history_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textPrimary,
                )
                Text(
                    text = stringResource(R.string.settings_history_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                TextButton(
                    onClick = {
                        haptic(HapticFeedbackType.TextHandleMove)
                        showClearDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(CalculinoSizes.iconSmall),
                    )
                    Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                    Text(
                        text = stringResource(R.string.settings_clear_history),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            SettingsSection(
                title = stringResource(R.string.settings_section_ai),
                icon = Icons.Rounded.SmartToy,
            ) {
                Text(
                    text = stringResource(R.string.settings_ai_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textPrimary,
                )
                Text(
                    text = stringResource(R.string.settings_ai_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                SettingSwitchRow(
                    title = stringResource(R.string.settings_local_ai),
                    subtitle = stringResource(R.string.settings_local_ai_description),
                    checked = settings.localAiEnabled,
                    enabled = true,
                    onCheckedChange = onLocalAiChange,
                )
                if (settings.localAiEnabled) {
                    Text(
                        text = stringResource(R.string.ai_model_qwen25_name),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
                SettingSwitchRow(
                    title = stringResource(R.string.settings_camera_ocr),
                    subtitle = stringResource(R.string.settings_camera_ocr_description),
                    checked = settings.cameraOcrEnabled,
                    enabled = true,
                    onCheckedChange = onCameraOcrChange,
                )
                if (settings.localAiEnabled) {
                    TextButton(
                        onClick = {
                            haptic(HapticFeedbackType.TextHandleMove)
                            onOpenAi()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(CalculinoSizes.iconSmall),
                        )
                        Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                        Text(stringResource(R.string.settings_ai_open))
                    }
                }
            }
            SettingsSection(
                title = stringResource(R.string.settings_section_about),
                icon = Icons.Rounded.Info,
            ) {
                Text(
                    text = stringResource(R.string.settings_about_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.title,
                )
                Text(
                    text = stringResource(R.string.settings_about_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                TextButton(
                    onClick = {
                        haptic(HapticFeedbackType.TextHandleMove)
                        onOpenAbout()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(CalculinoSizes.iconSmall),
                    )
                    Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                    Text(stringResource(R.string.settings_about_open))
                }
            }
            Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_dialog_title)) },
            text = { Text(stringResource(R.string.settings_clear_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic(HapticFeedbackType.TextHandleMove)
                        showClearDialog = false
                        onClearHistory()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.settings_clear_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsHeader() {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        Surface(
            shape = CircleShape,
            color = palette.surfaceMuted,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier.size(CalculinoSizes.minTouchTarget),
        ) {
            Box(contentAlignment = Alignment.Center) {
                MushroomIcon(
                    capColor = palette.title,
                    stemColor = palette.title.copy(alpha = 0.75f),
                    dotColor = palette.surface,
                    modifier = Modifier.size(CalculinoSizes.iconLarge),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = palette.title,
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.title,
                    modifier = Modifier.size(CalculinoSizes.iconMedium),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    val shape = PillShape
    Box(
        modifier = modifier
            .heightIn(min = CalculinoSizes.minTouchTarget)
            .clip(shape)
            .background(if (selected) palette.keyMultiply else palette.surfaceMuted)
            .border(
                width = if (selected) CalculinoSizes.focusBorder else CalculinoSizes.hairline,
                color = if (selected) palette.focusRing else palette.border,
                shape = shape,
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .padding(horizontal = CalculinoSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = palette.onKeyMultiply,
                    modifier = Modifier.size(CalculinoSizes.iconSmall),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) palette.onKeyMultiply else palette.textSecondary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.55f)
            .clip(MaterialTheme.shapes.large)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
            ) { newValue ->
                haptic(HapticFeedbackType.TextHandleMove)
                onCheckedChange(newValue)
            }
            .padding(vertical = CalculinoSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.onKeyPlus,
                checkedTrackColor = palette.keyPlus,
                uncheckedThumbColor = palette.icon,
                uncheckedTrackColor = palette.surfaceMuted,
                uncheckedBorderColor = palette.border,
            ),
        )
    }
}

@Composable
private fun themeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.System -> R.string.settings_theme_system
        ThemeMode.Light -> R.string.settings_theme_light
        ThemeMode.Dark -> R.string.settings_theme_dark
    },
)
