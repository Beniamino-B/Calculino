package com.calculino.app.ui.algebra

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.data.mathjs.MathJsApiRepository
import com.calculino.app.domain.algebra.AlgebraError
import com.calculino.app.domain.algebra.AlgebraResult
import com.calculino.app.domain.algebra.AlgebraSolution
import com.calculino.app.domain.math.MathStep
import com.calculino.app.domain.mathjs.MathJsError
import com.calculino.app.domain.mathjs.MathJsOutcome
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun AlgebraRoute(
    mathJsRepository: MathJsApiRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AlgebraViewModel = viewModel(
        initializer = { AlgebraViewModel(mathJsRepository) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AlgebraScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onExample = viewModel::onExample,
        onModeChange = viewModel::onModeChange,
        onPrecisionChange = viewModel::onPrecisionChange,
        onSolve = viewModel::solve,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun AlgebraScreen(
    state: AlgebraUiState,
    onInputChange: (String) -> Unit,
    onExample: (String) -> Unit,
    onModeChange: (AlgebraMode) -> Unit,
    onPrecisionChange: (Int) -> Unit,
    onSolve: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val examples = listOf(
        stringResource(R.string.algebra_example_linear),
        stringResource(R.string.algebra_example_quadratic),
        stringResource(R.string.algebra_example_system),
    )
    val exampleLabels = listOf(
        stringResource(R.string.algebra_example_linear),
        stringResource(R.string.algebra_example_quadratic),
        stringResource(R.string.algebra_example_system_label),
    )

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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = CalculinoSpacing.lg,
                    vertical = CalculinoSpacing.sm,
                ),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            ScienceToolHeader(
                title = stringResource(R.string.algebra_title),
                subtitle = stringResource(R.string.algebra_subtitle),
                onBack = onBack,
            )
            TabRow(
                selectedTabIndex = state.mode.ordinal,
                containerColor = palette.surface,
                contentColor = palette.title,
            ) {
                AlgebraMode.entries.forEach { mode ->
                    Tab(
                        selected = state.mode == mode,
                        onClick = { onModeChange(mode) },
                        text = {
                            Text(
                                stringResource(
                                    if (mode == AlgebraMode.Local) {
                                        R.string.algebra_mode_local
                                    } else {
                                        R.string.algebra_mode_online
                                    },
                                ),
                            )
                        },
                    )
                }
            }
            if (state.mode == AlgebraMode.Online) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    precisionOptions.forEach { precision ->
                        FilterChip(
                            selected = state.precision == precision,
                            onClick = { onPrecisionChange(precision) },
                            label = { Text(stringResource(R.string.algebra_precision, precision)) },
                        )
                    }
                }
            }
            DottedMathField(
                value = state.input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.algebra_system_hint),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            AdvancedMathKeyboard(
                onInsert = { token -> onInputChange(state.input + token) },
                onBackspace = { onInputChange(state.input.dropLast(1)) },
                onClear = { onInputChange("") },
            )
            Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
                Text(
                    text = stringResource(R.string.algebra_examples),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    examples.forEachIndexed { index, example ->
                        AssistChip(
                            onClick = { onExample(example) },
                            label = { Text(exampleLabels[index], maxLines = 1) },
                        )
                    }
                }
            }
            Button(
                onClick = onSolve,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.input.isNotBlank() && !state.onlineLoading,
            ) {
                if (state.onlineLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(CalculinoSizes.iconMedium),
                        strokeWidth = CalculinoSizes.focusBorder,
                    )
                } else {
                    Text(
                        stringResource(
                            if (state.mode == AlgebraMode.Online) {
                                R.string.algebra_solve_online
                            } else {
                                R.string.algebra_solve
                            },
                        ),
                    )
                }
            }
            if (state.mode == AlgebraMode.Local) {
                state.result?.let { result ->
                    when (result) {
                        is AlgebraResult.Success -> SolutionContent(
                            solution = result.solution,
                        )
                        is AlgebraResult.Failure -> ErrorContent(error = result.error)
                    }
                }
            } else {
                OnlineResultContent(outcome = state.onlineOutcome)
            }
        }
    }
}

@Composable
private fun OnlineResultContent(outcome: MathJsOutcome?) {
    val palette = LocalKawaiiPalette.current
    when (outcome) {
        is MathJsOutcome.Success -> Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = palette.keyClear,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .kawaiiCard(),
        ) {
            Column(
                modifier = Modifier.padding(CalculinoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.algebra_online_result),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.onKeyClear.copy(alpha = 0.75f),
                )
                Text(
                    text = outcome.value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = palette.onKeyClear,
                )
                if (outcome.cached) {
                    Text(
                        text = stringResource(R.string.algebra_online_cached),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.onKeyClear.copy(alpha = 0.75f),
                    )
                }
            }
        }
        is MathJsOutcome.Failure -> Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = palette.surface,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .kawaiiCard(),
        ) {
            Column(
                modifier = Modifier.padding(CalculinoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.algebra_online_error),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(
                        when (outcome.error) {
                            MathJsError.Offline -> R.string.algebra_online_offline
                            MathJsError.Timeout -> R.string.algebra_online_timeout
                            MathJsError.Network -> R.string.algebra_online_network
                            MathJsError.InvalidExpression -> R.string.algebra_online_invalid
                            MathJsError.EmptyResponse -> R.string.algebra_online_empty
                            MathJsError.Server -> R.string.algebra_online_server
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                outcome.detail?.let { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = palette.textSecondary,
                    )
                }
            }
        }
        null -> Unit
    }
}

@Composable
private fun SolutionContent(solution: AlgebraSolution) {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = palette.keyClear,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .kawaiiCard(),
        ) {
            Column(
                modifier = Modifier.padding(CalculinoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.algebra_solution),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.onKeyClear.copy(alpha = 0.75f),
                )
                Text(
                    text = solution.headline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.onKeyClear,
                )
            }
        }
        StepsContent(steps = solution.steps)
    }
}

@Composable
private fun StepsContent(steps: List<MathStep>) {
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
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.algebra_steps),
                style = MaterialTheme.typography.titleMedium,
                color = palette.title,
            )
            steps.forEachIndexed { index, step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = palette.surfaceMuted,
                        modifier = Modifier.size(CalculinoSizes.iconMedium),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.title,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
                    ) {
                        Text(
                            text = step.expression,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = palette.textPrimary,
                        )
                        Text(
                            text = step.result,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(error: AlgebraError) {
    val message = stringResource(
        when (error) {
            AlgebraError.InvalidInput -> R.string.algebra_error_invalid
            AlgebraError.Unsupported -> R.string.algebra_error_unsupported
            AlgebraError.NoRealSolution -> R.string.algebra_error_no_real
            AlgebraError.NoUniqueSolution -> R.string.algebra_error_no_unique
            AlgebraError.InfiniteSolutions -> R.string.algebra_error_infinite
        },
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(CalculinoSpacing.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

private val precisionOptions = listOf(2, 4, 6, 8, 12)
