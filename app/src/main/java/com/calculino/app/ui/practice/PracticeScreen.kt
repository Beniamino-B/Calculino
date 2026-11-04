package com.calculino.app.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.practice.PracticeCategory
import com.calculino.app.domain.practice.PracticeDifficulty
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.PawIcon
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape

@Composable
fun PracticeRoute(
    modifier: Modifier = Modifier,
) {
    val viewModel: PracticeViewModel = viewModel(
        initializer = { PracticeViewModel(createSavedStateHandle()) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PracticeScreen(
        state = state,
        onCategoryChange = viewModel::onCategoryChange,
        onDifficultyChange = viewModel::onDifficultyChange,
        onAnswerChange = viewModel::onAnswerChange,
        onCheck = viewModel::onCheck,
        onRetry = viewModel::onRetry,
        onNext = viewModel::onNext,
        onHint = viewModel::onRevealHint,
        onToggleSteps = viewModel::onToggleSteps,
        modifier = modifier,
    )
}

@Composable
fun PracticeScreen(
    state: PracticeUiState,
    onCategoryChange: (PracticeCategory) -> Unit,
    onDifficultyChange: (PracticeDifficulty) -> Unit,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onHint: () -> Unit,
    onToggleSteps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current

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
                .padding(horizontal = CalculinoSpacing.lg)
                .imePadding(),
        ) {
            PracticeHeader()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = CalculinoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                CategorySelector(
                    selected = state.category,
                    onSelect = onCategoryChange,
                )
                DifficultySelector(
                    selected = state.difficulty,
                    onSelect = onDifficultyChange,
                )
                StatsRow(state)
                ExerciseCard(
                    state = state,
                    onAnswerChange = onAnswerChange,
                    onCheck = onCheck,
                    onRetry = onRetry,
                    onNext = onNext,
                    onHint = onHint,
                    onToggleSteps = onToggleSteps,
                )
                Text(
                    text = stringResource(R.string.practice_session_note, state.hintsUsed),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
            }
        }
    }
}

@Composable
private fun PracticeHeader() {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        PawIcon(
            color = palette.decorationAccent,
            modifier = Modifier.size(CalculinoSizes.decorationMedium),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.practice_title),
                style = MaterialTheme.typography.headlineLarge,
                color = palette.title,
            )
            Text(
                text = stringResource(R.string.practice_description),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
        Surface(
            shape = CircleShape,
            color = palette.surfaceMuted,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier.size(CalculinoSizes.minTouchTarget),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    tint = palette.title,
                    modifier = Modifier.size(CalculinoSizes.iconMedium),
                )
            }
        }
    }
}

@Composable
private fun CategorySelector(
    selected: PracticeCategory,
    onSelect: (PracticeCategory) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
        Text(
            text = stringResource(R.string.practice_section_category),
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            PracticeCategory.entries.forEach { category ->
                PracticeChoiceChip(
                    label = categoryLabel(category),
                    selected = selected == category,
                    onClick = { onSelect(category) },
                )
            }
        }
    }
}

@Composable
private fun DifficultySelector(
    selected: PracticeDifficulty,
    onSelect: (PracticeDifficulty) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
        Text(
            text = stringResource(R.string.practice_section_difficulty),
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            PracticeDifficulty.entries.forEach { difficulty ->
                PracticeChoiceChip(
                    label = difficultyLabel(difficulty),
                    selected = selected == difficulty,
                    onClick = { onSelect(difficulty) },
                )
            }
        }
    }
}

@Composable
private fun PracticeChoiceChip(
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
            .padding(horizontal = CalculinoSpacing.sm),
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
private fun StatsRow(state: PracticeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            StatCard(
                value = state.solved.toString(),
                label = stringResource(R.string.practice_stats_solved),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = state.attempts.toString(),
                label = stringResource(R.string.practice_stats_attempts),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            StatCard(
                value = state.streak.toString(),
                label = stringResource(R.string.practice_stats_streak),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = state.bestStreak.toString(),
                label = stringResource(R.string.practice_stats_best),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surfaceMuted,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = modifier.kawaiiCard(MaterialTheme.shapes.large),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = CalculinoSpacing.xs,
                vertical = CalculinoSpacing.sm,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = palette.title,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    state: PracticeUiState,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onHint: () -> Unit,
    onToggleSteps: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    val focusManager = LocalFocusManager.current

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
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
                PracticeTag(categoryLabel(state.category))
                PracticeTag(difficultyLabel(state.difficulty))
            }
            Text(
                text = state.exercise.prompt,
                style = MaterialTheme.typography.headlineLarge,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.category == PracticeCategory.Equations) {
                Text(
                    text = stringResource(R.string.practice_equation_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnswerField(
                state = state,
                onAnswerChange = onAnswerChange,
                onCheck = {
                    focusManager.clearFocus()
                    onCheck()
                },
            )
            HintSection(
                state = state,
                onHint = onHint,
            )
            when (state.feedback) {
                PracticeFeedback.Correct -> FeedbackCard(
                    correct = true,
                    title = stringResource(R.string.practice_correct_title),
                    body = stringResource(
                        R.string.practice_correct_body,
                        state.exercise.answerText,
                    ),
                )
                PracticeFeedback.Incorrect -> FeedbackCard(
                    correct = false,
                    title = stringResource(R.string.practice_incorrect_title),
                    body = stringResource(R.string.practice_incorrect_body),
                )
                PracticeFeedback.None -> Unit
            }
            when (state.feedback) {
                PracticeFeedback.None -> Button(
                    shape = PillShape,
                    onClick = {
                        haptic(HapticFeedbackType.TextHandleMove)
                        onCheck()
                    },
                    enabled = state.canCheck,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = CalculinoSizes.minTouchTarget),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.keyPlus,
                        contentColor = palette.onKeyPlus,
                        disabledContainerColor = palette.surfaceStrong,
                        disabledContentColor = palette.textSecondary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(CalculinoSizes.iconSmall),
                    )
                    Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                    Text(stringResource(R.string.practice_check))
                }
                PracticeFeedback.Correct -> {
                    Button(
                    shape = PillShape,
                        onClick = {
                            haptic(HapticFeedbackType.TextHandleMove)
                            onNext()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = CalculinoSizes.minTouchTarget),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.keyPlus,
                            contentColor = palette.onKeyPlus,
                        ),
                    ) {
                        Text(stringResource(R.string.practice_next))
                    }
                    StepsToggle(
                        visible = state.stepsVisible,
                        onToggle = {
                            haptic(HapticFeedbackType.TextHandleMove)
                            onToggleSteps()
                        },
                    )
                }
                PracticeFeedback.Incorrect -> {
                    if (state.stepsVisible) {
                        Button(
                    shape = PillShape,
                            onClick = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onNext()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = CalculinoSizes.minTouchTarget),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.keyPlus,
                                contentColor = palette.onKeyPlus,
                            ),
                        ) {
                            Text(stringResource(R.string.practice_next))
                        }
                        TextButton(
                            onClick = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onRetry()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = CalculinoSizes.minTouchTarget),
                        ) {
                            Text(stringResource(R.string.practice_retry))
                        }
                        StepsToggle(
                            visible = state.stepsVisible,
                            onToggle = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onToggleSteps()
                            },
                        )
                    } else {
                        Button(
                    shape = PillShape,
                            onClick = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onRetry()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = CalculinoSizes.minTouchTarget),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.keyDivide,
                                contentColor = palette.onKeyDivide,
                            ),
                        ) {
                            Text(stringResource(R.string.practice_retry))
                        }
                        StepsToggle(
                            visible = state.stepsVisible,
                            onToggle = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onToggleSteps()
                            },
                        )
                    }
                }
            }
            if (state.stepsVisible) {
                StepsContent(steps = state.exercise.steps)
            }
        }
    }
}

@Composable
private fun PracticeTag(label: String) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surfaceMuted,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary,
            modifier = Modifier.padding(
                horizontal = CalculinoSpacing.sm,
                vertical = CalculinoSpacing.xxs,
            ),
        )
    }
}

@Composable
private fun AnswerField(
    state: PracticeUiState,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val focusManager = LocalFocusManager.current
    val slashDescription = stringResource(R.string.practice_fraction_key)
    val enabled = state.feedback != PracticeFeedback.Correct

    OutlinedTextField(
        value = state.answerInput,
        onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(stringResource(R.string.practice_answer_label)) },
        placeholder = { Text(stringResource(R.string.practice_answer_placeholder)) },
        singleLine = true,
        isError = state.inputError,
        supportingText = {
            Text(
                text = stringResource(
                    if (state.inputError) {
                        R.string.practice_answer_invalid
                    } else {
                        R.string.practice_answer_format
                    },
                ),
                color = if (state.inputError) {
                    MaterialTheme.colorScheme.error
                } else {
                    palette.textSecondary
                },
            )
        },
        shape = MaterialTheme.shapes.large,
        trailingIcon = {
            IconButton(
                onClick = { onAnswerChange(state.answerInput + "/") },
                enabled = enabled,
                modifier = Modifier.semantics { contentDescription = slashDescription },
            ) {
                Text(
                    text = stringResource(R.string.practice_slash),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled) palette.title else palette.textSecondary,
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onCheck()
            },
        ),
    )
}

@Composable
private fun HintSection(
    state: PracticeUiState,
    onHint: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    if (state.hintsRevealed == 0 && !state.canRevealHint) return
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
        repeat(state.hintsRevealed) { index ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = palette.keyCurrency,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(CalculinoSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = palette.onKeyCurrency,
                        modifier = Modifier.size(CalculinoSizes.iconSmall),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
                        Text(
                            text = stringResource(R.string.practice_hint_label, index + 1),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.onKeyCurrency,
                        )
                        Text(
                            text = state.exercise.hints[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.onKeyCurrency,
                        )
                    }
                }
            }
        }
        if (state.canRevealHint) {
            TextButton(
                onClick = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onHint()
                },
                modifier = Modifier.heightIn(min = CalculinoSizes.minTouchTarget),
                contentPadding = PaddingValues(horizontal = CalculinoSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(CalculinoSizes.iconSmall),
                )
                Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                Text(
                    stringResource(
                        if (state.hintsRevealed == 0) {
                            R.string.practice_hint_show
                        } else {
                            R.string.practice_hint_more
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    correct: Boolean,
    title: String,
    body: String,
) {
    val palette = LocalKawaiiPalette.current
    val container = if (correct) palette.keyPlus else palette.keyDivide
    val content = if (correct) palette.onKeyPlus else palette.onKeyDivide
    Surface(
        shape = MaterialTheme.shapes.large,
        color = container,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(CalculinoSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Icon(
                imageVector = if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(CalculinoSizes.iconMedium),
            )
            Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = content,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = content,
                )
            }
        }
    }
}

@Composable
private fun StepsContent(steps: List<String>) {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm)) {
        Text(
            text = stringResource(R.string.practice_steps_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.title,
        )
        steps.forEachIndexed { index, step ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = palette.surfaceMuted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(CalculinoSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(CalculinoSizes.stepNumberSize)
                            .clip(CircleShape)
                            .background(palette.keyMultiply),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.onKeyMultiply,
                        )
                    }
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsToggle(
    visible: Boolean,
    onToggle: () -> Unit,
) {
    TextButton(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CalculinoSizes.minTouchTarget),
    ) {
        Text(
            stringResource(
                if (visible) R.string.practice_hide_steps else R.string.practice_show_steps,
            ),
        )
    }
}

@Composable
private fun categoryLabel(category: PracticeCategory): String = stringResource(
    when (category) {
        PracticeCategory.Arithmetic -> R.string.practice_category_arithmetic
        PracticeCategory.Fractions -> R.string.practice_category_fractions
        PracticeCategory.Powers -> R.string.practice_category_powers
        PracticeCategory.Equations -> R.string.practice_category_equations
    },
)

@Composable
private fun difficultyLabel(difficulty: PracticeDifficulty): String = stringResource(
    when (difficulty) {
        PracticeDifficulty.Easy -> R.string.practice_difficulty_easy
        PracticeDifficulty.Medium -> R.string.practice_difficulty_medium
        PracticeDifficulty.Hard -> R.string.practice_difficulty_hard
    },
)
