package com.calculino.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.calculino.app.R
import com.calculino.app.domain.ai.LocalModel
import com.calculino.app.domain.settings.ThemeMode
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.SparkleIcon
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

private enum class OnboardingPage {
    Welcome,
    Theme,
    LocalAi,
    Model,
    Camera,
    Ready,
}


@Composable
fun OnboardingScreen(
    initialThemeMode: ThemeMode,
    initialLocalAiEnabled: Boolean,
    initialCameraOcrEnabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLocalAiSelected: (Boolean) -> Unit,
    onLocalAiModelSelected: (String) -> Unit,
    onCameraOcrSelected: (Boolean) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    var page by rememberSaveable { mutableStateOf(OnboardingPage.Welcome) }
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    var localAiEnabled by rememberSaveable { mutableStateOf(initialLocalAiEnabled) }
    var cameraOcrEnabled by rememberSaveable { mutableStateOf(initialCameraOcrEnabled) }

    fun nextPage() {
        page = when (page) {
            OnboardingPage.Welcome -> OnboardingPage.Theme
            OnboardingPage.Theme -> OnboardingPage.LocalAi
            OnboardingPage.LocalAi -> if (localAiEnabled) OnboardingPage.Model else OnboardingPage.Camera
            OnboardingPage.Model -> OnboardingPage.Camera
            OnboardingPage.Camera -> OnboardingPage.Ready
            OnboardingPage.Ready -> OnboardingPage.Ready
        }
    }

    fun previousPage() {
        page = when (page) {
            OnboardingPage.Welcome -> OnboardingPage.Welcome
            OnboardingPage.Theme -> OnboardingPage.Welcome
            OnboardingPage.LocalAi -> OnboardingPage.Theme
            OnboardingPage.Model -> OnboardingPage.LocalAi
            OnboardingPage.Camera -> if (localAiEnabled) OnboardingPage.Model else OnboardingPage.LocalAi
            OnboardingPage.Ready -> OnboardingPage.Camera
        }
    }

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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = CalculinoSpacing.xl, vertical = CalculinoSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (page == OnboardingPage.Welcome) {
                WelcomeContent()
            } else {
                WizardContent(
                    page = page,
                    themeMode = themeMode,
                    localAiEnabled = localAiEnabled,
                    cameraOcrEnabled = cameraOcrEnabled,
                    onThemeModeSelected = { mode ->
                        themeMode = mode
                        onThemeModeSelected(mode)
                    },
                    onLocalAiSelected = { enabled ->
                        localAiEnabled = enabled
                        onLocalAiSelected(enabled)
                    },
                    onCameraOcrSelected = { enabled ->
                        cameraOcrEnabled = enabled
                        onCameraOcrSelected(enabled)
                    },
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (page != OnboardingPage.Welcome) {
                PageIndicator(page = page, localAiEnabled = localAiEnabled)
                Spacer(modifier = Modifier.size(CalculinoSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    TextButton(
                        onClick = ::previousPage,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.onboarding_back))
                    }
                    Button(
                        onClick = {
                            if (page == OnboardingPage.Ready) {
                                onLocalAiModelSelected(LocalModel.Qwen2_5_1_5B.id)
                                onComplete()
                            } else {
                                nextPage()
                            }
                        },
                        modifier = Modifier.weight(1.4f),
                    ) {
                        Text(
                            stringResource(
                                if (page == OnboardingPage.Ready) {
                                    R.string.onboarding_finish
                                } else {
                                    R.string.onboarding_continue
                                },
                            ),
                        )
                    }
                }
            } else {
                Button(
                    onClick = ::nextPage,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_start))
                }
            }
            Spacer(modifier = Modifier.size(CalculinoSpacing.md))
            Text(
                text = stringResource(R.string.onboarding_footer),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ColumnScope.WelcomeContent() {
    val palette = LocalKawaiiPalette.current
    Spacer(modifier = Modifier.weight(1f))
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier.size(CalculinoSizes.heroIconContainer),
    ) {
        Box(contentAlignment = Alignment.Center) {
            MushroomIcon(
                capColor = palette.title,
                stemColor = palette.title.copy(alpha = 0.75f),
                dotColor = palette.surface,
                modifier = Modifier.size(CalculinoSizes.heroIcon),
            )
            SparkleIcon(
                color = palette.decorationAccent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(CalculinoSpacing.sm)
                    .size(CalculinoSizes.decorationSmall),
            )
        }
    }
    Spacer(modifier = Modifier.size(CalculinoSpacing.xl))
    Text(
        text = stringResource(R.string.onboarding_title),
        style = MaterialTheme.typography.headlineLarge,
        color = palette.title,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.size(CalculinoSpacing.sm))
    Text(
        text = stringResource(R.string.onboarding_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = palette.textSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.weight(1.35f))
}

@Composable
private fun ColumnScope.WizardContent(
    page: OnboardingPage,
    themeMode: ThemeMode,
    localAiEnabled: Boolean,
    cameraOcrEnabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLocalAiSelected: (Boolean) -> Unit,
    onCameraOcrSelected: (Boolean) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val title = stringResource(
        when (page) {
            OnboardingPage.Theme -> R.string.onboarding_theme_title
            OnboardingPage.LocalAi -> R.string.onboarding_local_ai_title
            OnboardingPage.Model -> R.string.onboarding_model_title
            OnboardingPage.Camera -> R.string.onboarding_camera_title
            else -> R.string.onboarding_ready_title
        },
    )
    val subtitle = stringResource(
        when (page) {
            OnboardingPage.Theme -> R.string.onboarding_theme_subtitle
            OnboardingPage.LocalAi -> R.string.onboarding_local_ai_subtitle
            OnboardingPage.Model -> R.string.onboarding_model_subtitle
            OnboardingPage.Camera -> R.string.onboarding_camera_subtitle
            else -> R.string.onboarding_ready_subtitle
        },
    )
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = palette.title,
        textAlign = TextAlign.Center,
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = palette.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = CalculinoSpacing.sm),
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = CalculinoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        when (page) {
            OnboardingPage.Theme -> {
                ThemeMode.entries.forEach { mode ->
                    ChoiceCard(
                        title = themeLabel(mode),
                        subtitle = stringResource(
                            when (mode) {
                                ThemeMode.System -> R.string.onboarding_theme_auto_description
                                ThemeMode.Light -> R.string.onboarding_theme_light_description
                                ThemeMode.Dark -> R.string.onboarding_theme_dark_description
                            },
                        ),
                        selected = themeMode == mode,
                        onClick = { onThemeModeSelected(mode) },
                    )
                }
            }
            OnboardingPage.LocalAi -> {
                ChoiceCard(
                    title = stringResource(R.string.onboarding_yes),
                    subtitle = stringResource(R.string.onboarding_local_ai_yes_description),
                    selected = localAiEnabled,
                    onClick = { onLocalAiSelected(true) },
                )
                ChoiceCard(
                    title = stringResource(R.string.onboarding_no),
                    subtitle = stringResource(R.string.onboarding_local_ai_no_description),
                    selected = !localAiEnabled,
                    onClick = { onLocalAiSelected(false) },
                )
                InfoNotice(text = stringResource(R.string.onboarding_local_ai_note))
            }
            OnboardingPage.Model -> {
                ChoiceCard(
                    title = stringResource(R.string.ai_model_qwen25_name),
                    subtitle = stringResource(R.string.onboarding_model_description),
                    selected = true,
                    onClick = {},
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(CalculinoSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(CalculinoSizes.iconMedium),
                        )
                        Text(
                            text = stringResource(R.string.onboarding_model_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            OnboardingPage.Camera -> {
                ChoiceCard(
                    title = stringResource(R.string.onboarding_yes),
                    subtitle = stringResource(R.string.onboarding_camera_yes_description),
                    selected = cameraOcrEnabled,
                    onClick = { onCameraOcrSelected(true) },
                )
                ChoiceCard(
                    title = stringResource(R.string.onboarding_no),
                    subtitle = stringResource(R.string.onboarding_camera_no_description),
                    selected = !cameraOcrEnabled,
                    onClick = { onCameraOcrSelected(false) },
                )
                InfoNotice(text = stringResource(R.string.onboarding_camera_note))
            }
            else -> {
                SummaryRow(
                    label = stringResource(R.string.onboarding_summary_theme),
                    value = themeLabel(themeMode),
                )
                SummaryRow(
                    label = stringResource(R.string.onboarding_summary_local_ai),
                    value = stringResource(
                        if (localAiEnabled) R.string.onboarding_enabled else R.string.onboarding_disabled,
                    ),
                )
                if (localAiEnabled) {
                    SummaryRow(
                        label = stringResource(R.string.onboarding_summary_model),
                        value = stringResource(R.string.ai_model_qwen25_name),
                    )
                }
                SummaryRow(
                    label = stringResource(R.string.onboarding_summary_camera),
                    value = stringResource(
                        if (cameraOcrEnabled) R.string.onboarding_enabled else R.string.onboarding_disabled,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) palette.keyClear else palette.surface,
        border = BorderStroke(
            CalculinoSizes.hairline,
            if (selected) palette.border else palette.border.copy(alpha = 0.5f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard()
            .clickable(role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) palette.onKeyClear else palette.textPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        palette.onKeyClear.copy(alpha = 0.8f)
                    } else {
                        palette.textSecondary
                    },
                )
            }
            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = palette.onKeyClear,
                    modifier = Modifier.size(CalculinoSizes.iconMedium),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = palette.keyClear,
                            modifier = Modifier.size(CalculinoSizes.iconSmall),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoNotice(text: String) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surfaceMuted,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            modifier = Modifier.padding(CalculinoSpacing.md),
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CalculinoSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = palette.title,
        )
    }
}

@Composable
private fun PageIndicator(
    page: OnboardingPage,
    localAiEnabled: Boolean,
) {
    val pages = buildList {
        add(OnboardingPage.Theme)
        add(OnboardingPage.LocalAi)
        if (localAiEnabled) add(OnboardingPage.Model)
        add(OnboardingPage.Camera)
        add(OnboardingPage.Ready)
    }
    val currentIndex = pages.indexOf(page).coerceAtLeast(0)
    Row(horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
        pages.forEachIndexed { index, _ ->
            Surface(
                shape = CircleShape,
                color = if (index == currentIndex) {
                    LocalKawaiiPalette.current.title
                } else {
                    LocalKawaiiPalette.current.border
                },
                modifier = Modifier.size(
                    if (index == currentIndex) {
                        CalculinoSizes.pageIndicatorActive
                    } else {
                        CalculinoSizes.pageIndicatorInactive
                    },
                ),
            ) {}
        }
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
