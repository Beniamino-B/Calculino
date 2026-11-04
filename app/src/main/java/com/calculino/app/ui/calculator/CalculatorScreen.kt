package com.calculino.app.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.data.currency.CurrencyRepository
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.ai.VerifiedCalculation
import com.calculino.app.domain.math.AngleMode
import com.calculino.app.domain.math.LongDivisionProcedure
import com.calculino.app.domain.math.MathError
import com.calculino.app.domain.math.MathEvaluationResult
import com.calculino.app.ui.procedure.LongDivisionSheet
import com.calculino.app.ui.components.ElasticSheetContent
import com.calculino.app.ui.components.ExactnessBadge
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.LeafIcon
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.PawIcon
import com.calculino.app.ui.components.SparkleIcon
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.engineering.CurrencySheet
import com.calculino.app.ui.history.HistorySheet
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape

@Composable
fun CalculatorRoute(
    historyRepository: HistoryRepository,
    currencyRepository: CurrencyRepository,
    scannedExpression: String,
    cameraOcrEnabled: Boolean,
    onOpenScanner: () -> Unit,
    onScannedExpressionConsumed: () -> Unit,
    onExplain: (VerifiedCalculation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CalculatorViewModel = viewModel(
        initializer = {
            CalculatorViewModel(
                savedStateHandle = createSavedStateHandle(),
                historyRepository = historyRepository,
            )
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(scannedExpression) {
        if (scannedExpression.isNotEmpty()) {
            viewModel.onScannedExpression(scannedExpression)
            onScannedExpressionConsumed()
        }
    }

    CalculatorScreen(
        state = state,
        historyRepository = historyRepository,
        currencyRepository = currencyRepository,
        cameraOcrEnabled = cameraOcrEnabled,
        onOpenScanner = onOpenScanner,
        onToken = viewModel::onToken,
        onToggleSign = viewModel::onToggleSign,
        onToggleAngleMode = viewModel::onToggleAngleMode,
        onClear = viewModel::onClear,
        onBackspace = viewModel::onBackspace,
        onEquals = viewModel::onEquals,
        onExplain = onExplain,
        modifier = modifier,
    )
}

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    historyRepository: HistoryRepository,
    currencyRepository: CurrencyRepository,
    cameraOcrEnabled: Boolean,
    onOpenScanner: () -> Unit,
    onToken: (String) -> Unit,
    onToggleSign: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onExplain: (VerifiedCalculation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val contentScroll = rememberScrollState()
    var showFunctions by rememberSaveable { mutableStateOf(false) }
    var showProcedure by rememberSaveable { mutableStateOf(false) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showCurrency by rememberSaveable { mutableStateOf(false) }
    val onSpecClick: (CalcKeySpec) -> Unit = { spec ->
        when (spec.kind) {
            CalcKeyKind.Clear -> onClear()
            CalcKeyKind.Delete -> onBackspace()
            CalcKeyKind.Equals -> onEquals()
            CalcKeyKind.Currency -> showCurrency = true
            CalcKeyKind.Function -> if (spec.token == null) onToggleSign() else onToken(spec.token)
            else -> spec.token?.let(onToken)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        KawaiiBackdrop()
        CalculatorWatermarks()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val expandedKeypad = maxHeight >= CalculinoSizes.calculatorExpandedMinHeight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (expandedKeypad) Modifier else Modifier.verticalScroll(contentScroll)),
                contentAlignment = Alignment.TopCenter,
            ) {
                CalculatorContent(
                    state = state,
                    onToggleAngleMode = onToggleAngleMode,
                    onOpenFunctions = { showFunctions = true },
                    onViewSteps = { showProcedure = true },
                    onExplain = onExplain,
                    onOpenHistory = { showHistory = true },
                    cameraOcrEnabled = cameraOcrEnabled,
                    onOpenScanner = onOpenScanner,
                    onSpecClick = onSpecClick,
                    expandKeypad = expandedKeypad,
                    modifier = Modifier
                        .widthIn(max = CalculinoSizes.screenMaxWidth)
                        .fillMaxWidth()
                        .then(if (expandedKeypad) Modifier.fillMaxSize() else Modifier),
                )
            }
        }
        if (showFunctions) {
            CalculatorFunctionsSheet(
                expression = state.expression,
                onInsert = onToken,
                onDismiss = { showFunctions = false },
            )
        }
        val procedure = state.procedure
        if (showProcedure && procedure != null) {
            LongDivisionSheet(
                procedure = procedure,
                onDismiss = { showProcedure = false },
            )
        }
        if (showHistory) {
            HistorySheet(
                historyRepository = historyRepository,
                onDismiss = { showHistory = false },
            )
        }
        if (showCurrency) {
            CurrencySheet(
                repository = currencyRepository,
                onDismiss = { showCurrency = false },
            )
        }
    }
}

@Composable
private fun CalculatorContent(
    state: CalculatorUiState,
    onToggleAngleMode: () -> Unit,
    onOpenFunctions: () -> Unit,
    onViewSteps: () -> Unit,
    onExplain: (VerifiedCalculation) -> Unit,
    onOpenHistory: () -> Unit,
    cameraOcrEnabled: Boolean,
    onOpenScanner: () -> Unit,
    onSpecClick: (CalcKeySpec) -> Unit,
    expandKeypad: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(
            horizontal = CalculinoSpacing.md,
            vertical = CalculinoSpacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        CalculatorHeader(
            onOpenHistory = onOpenHistory,
            cameraOcrEnabled = cameraOcrEnabled,
            onOpenScanner = onOpenScanner,
        )
        CalculatorDisplay(
            expression = state.expression,
            outcome = state.outcome,
            procedure = state.procedure,
            angleMode = state.angleMode,
            onToggleAngleMode = onToggleAngleMode,
            onOpenFunctions = onOpenFunctions,
            onViewSteps = onViewSteps,
            onExplain = onExplain,
            modifier = if (expandKeypad) Modifier.weight(0.3f) else Modifier.fillMaxWidth(),
        )
        CalculatorKeypad(
            canEvaluate = state.canEvaluate,
            onSpecClick = onSpecClick,
            expand = expandKeypad,
            modifier = if (expandKeypad) {
                Modifier.weight(0.7f)
            } else {
                Modifier.fillMaxWidth()
            },
        )
    }
}

@Composable
private fun CalculatorWatermarks() {
    val palette = LocalKawaiiPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics {},
    ) {
        MushroomIcon(
            capColor = palette.decorationAccent.copy(alpha = 0.08f),
            stemColor = palette.decoration.copy(alpha = 0.08f),
            dotColor = palette.surface.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = CalculinoSpacing.sm)
                .size(CalculinoSizes.decorationLarge)
                .rotate(14f),
        )
        PawIcon(
            color = palette.decorationAccent.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = CalculinoSpacing.md)
                .size(CalculinoSizes.decorationMedium)
                .rotate(-18f),
        )
        LeafIcon(
            color = palette.decoration.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CalculinoSpacing.xxl)
                .size(CalculinoSizes.decorationMedium)
                .rotate(58f),
        )
        SparkleIcon(
            color = palette.decoration.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = CalculinoSpacing.xxl)
                .size(CalculinoSizes.decorationSmall),
        )
    }
}

@Composable
private fun CalculatorHeader(
    onOpenHistory: () -> Unit,
    cameraOcrEnabled: Boolean,
    onOpenScanner: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val historyDescription = stringResource(R.string.calculator_open_history)
    val scannerDescription = stringResource(R.string.calculator_open_scanner)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = palette.title,
                )
                SparkleIcon(
                    color = palette.decorationAccent.copy(alpha = 0.65f),
                    modifier = Modifier.size(CalculinoSizes.iconSmall),
                )
            }
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
        if (cameraOcrEnabled) {
            Surface(
                shape = CircleShape,
                color = palette.surfaceMuted,
                border = BorderStroke(CalculinoSizes.hairline, palette.border),
                modifier = Modifier
                    .size(CalculinoSizes.minTouchTarget)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onOpenScanner)
                    .semantics { contentDescription = scannerDescription },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = palette.title,
                        modifier = Modifier.size(CalculinoSizes.iconLarge),
                    )
                }
            }
        }
        Surface(
            shape = CircleShape,
            color = palette.surfaceMuted,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier
                .size(CalculinoSizes.minTouchTarget)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onOpenHistory)
                .semantics { contentDescription = historyDescription },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = palette.title,
                    modifier = Modifier.size(CalculinoSizes.iconLarge),
                )
            }
        }
    }
}

@Composable
private fun CalculatorDisplay(
    expression: String,
    outcome: MathEvaluationResult?,
    procedure: LongDivisionProcedure?,
    angleMode: AngleMode,
    onToggleAngleMode: () -> Unit,
    onOpenFunctions: () -> Unit,
    onViewSteps: () -> Unit,
    onExplain: (VerifiedCalculation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val expressionScroll = rememberScrollState()
    val resultScroll = rememberScrollState()
    val display = expression.ifBlank { stringResource(R.string.calculator_zero) }
    val success = outcome as? MathEvaluationResult.Success
    val failure = outcome as? MathEvaluationResult.Failure
    val hasResult = outcome != null
    val angleLabel = stringResource(
        if (angleMode == AngleMode.Degrees) R.string.calculator_angle_degrees else R.string.calculator_angle_radians,
    )
    val angleDescription = stringResource(
        if (angleMode == AngleMode.Degrees) {
            R.string.calculator_angle_switch_to_radians
        } else {
            R.string.calculator_angle_switch_to_degrees
        },
    )

    LaunchedEffect(expression) {
        withFrameNanos { }
        expressionScroll.animateScrollTo(expressionScroll.maxValue)
    }
    LaunchedEffect(outcome) {
        withFrameNanos { }
        resultScroll.animateScrollTo(resultScroll.maxValue)
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = modifier
            .fillMaxWidth()
            .kawaiiCard()
            .heightIn(min = CalculinoSizes.displayMinHeight),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.calculator_display_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    DisplayChip(
                        label = angleLabel,
                        description = angleDescription,
                        containerColor = palette.surfaceMuted,
                        contentColor = palette.textSecondary,
                        onClick = onToggleAngleMode,
                    )
                    DisplayChip(
                        label = stringResource(R.string.calculator_functions_label),
                        description = stringResource(R.string.calculator_functions_open),
                        containerColor = palette.keyFunction,
                        contentColor = palette.onKeyFunction,
                        onClick = onOpenFunctions,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(modifier = Modifier.horizontalScroll(expressionScroll)) {
                    Text(
                        text = display,
                        style = if (hasResult) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.displayMedium
                        },
                        color = if (hasResult) palette.textSecondary else palette.textPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                    )
                }
            }
            when {
                success != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(resultScroll),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                        ) {
                            Text(
                                text = stringResource(
                                    if (success.display.isExact) {
                                        R.string.calculator_result_equals
                                    } else {
                                        R.string.calculator_result_approx
                                    },
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = palette.title,
                            )
                            Text(
                                text = success.display.main,
                                style = MaterialTheme.typography.displayMedium,
                                color = palette.textPrimary,
                                maxLines = 1,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                    ) {
                        ExactnessBadge(isExact = success.display.isExact)
                        val approximation = success.display.approximation
                        if (approximation != null) {
                            Text(
                                text = stringResource(R.string.calculator_approx_detail, approximation),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        if (procedure != null) {
                            DisplayChip(
                                label = stringResource(R.string.calculator_view_steps),
                                description = stringResource(R.string.calculator_view_steps_description),
                                containerColor = palette.keyPlus,
                                contentColor = palette.onKeyPlus,
                                onClick = onViewSteps,
                            )
                        }
                        DisplayChip(
                            label = stringResource(R.string.calculator_explain),
                            description = stringResource(R.string.calculator_explain_description),
                            containerColor = palette.keyNumber,
                            contentColor = palette.onKeyNumber,
                            onClick = {
                                onExplain(
                                    VerifiedCalculation(
                                        expression = expression,
                                        result = success.display.main,
                                        approximation = success.display.approximation,
                                        isExact = success.display.isExact,
                                        angleMode = angleMode,
                                    ),
                                )
                            },
                        )
                    }
                }
                failure != null -> Text(
                    text = errorMessage(failure.error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                else -> Text(
                    text = stringResource(
                        if (expression.isBlank()) {
                            R.string.calculator_empty_hint
                        } else {
                            R.string.calculator_ready_hint
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayChip(
    label: String,
    description: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHaptics()
    val shape = PillShape
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(CalculinoSizes.hairline, LocalKawaiiPalette.current.border, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                role = Role.Button,
                onClick = {
                    haptic(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .semantics { contentDescription = description }
            .heightIn(min = CalculinoSizes.minTouchTarget)
            .padding(
                horizontal = CalculinoSpacing.md,
                vertical = CalculinoSpacing.xs,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun CalculatorKeypad(
    canEvaluate: Boolean,
    onSpecClick: (CalcKeySpec) -> Unit,
    modifier: Modifier = Modifier,
    expand: Boolean = false,
) {
    val clearLabel = stringResource(R.string.calc_key_clear)
    val percentLabel = stringResource(R.string.calc_key_percent)
    val divideLabel = stringResource(R.string.calc_key_divide)
    val multiplyLabel = stringResource(R.string.calc_key_multiply)
    val minusLabel = stringResource(R.string.calc_key_minus)
    val plusLabel = stringResource(R.string.calc_key_plus)
    val decimalLabel = stringResource(R.string.calc_key_decimal)
    val equalsLabel = stringResource(R.string.calc_key_equals)
    val piLabel = stringResource(R.string.calc_key_pi)
    val sqrtLabel = stringResource(R.string.calc_key_sqrt)
    val powerLabel = stringResource(R.string.calc_key_power)
    val signLabel = stringResource(R.string.calc_key_sign)
    val currencyLabel = stringResource(R.string.calc_key_currency)

    val scientificRow = listOf(
        CalcKeySpec(piLabel, stringResource(R.string.calc_desc_pi), CalcKeyKind.Function, piLabel),
        CalcKeySpec(sqrtLabel, stringResource(R.string.calc_desc_sqrt), CalcKeyKind.Function, "$sqrtLabel("),
        CalcKeySpec(powerLabel, stringResource(R.string.calc_desc_power), CalcKeyKind.Function, powerLabel),
        CalcKeySpec(signLabel, stringResource(R.string.calc_desc_sign), CalcKeyKind.Function, null),
        CalcKeySpec(currencyLabel, stringResource(R.string.calc_desc_currency), CalcKeyKind.Currency, currencyLabel),
    )
    val rows = listOf(
        listOf(
            CalcKeySpec(clearLabel, stringResource(R.string.calc_desc_clear), CalcKeyKind.Clear),
            CalcKeySpec("", stringResource(R.string.calc_desc_delete), CalcKeyKind.Delete),
            CalcKeySpec(percentLabel, stringResource(R.string.calc_desc_percent), CalcKeyKind.Percent, percentLabel),
            CalcKeySpec(divideLabel, stringResource(R.string.calc_desc_divide), CalcKeyKind.Divide, divideLabel),
        ),
        digitRow(7, 8, 9, multiplyLabel, stringResource(R.string.calc_desc_multiply), CalcKeyKind.Multiply),
        digitRow(4, 5, 6, minusLabel, stringResource(R.string.calc_desc_minus), CalcKeyKind.Minus),
        digitRow(1, 2, 3, plusLabel, stringResource(R.string.calc_desc_plus), CalcKeyKind.Plus),
        listOf(
            CalcKeySpec(
                label = "0",
                description = stringResource(R.string.calc_desc_digit, "0"),
                kind = CalcKeyKind.Digit,
                token = "0",
                span = 2,
            ),
            CalcKeySpec(decimalLabel, stringResource(R.string.calc_desc_decimal), CalcKeyKind.Decimal, decimalLabel),
            CalcKeySpec(equalsLabel, stringResource(R.string.calc_desc_equals), CalcKeyKind.Equals),
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CalculinoSizes.keyGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expand) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.height(CalculinoSizes.scientificKeyHeight)
                    },
                ),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSizes.keyGap),
        ) {
            scientificRow.forEach { spec ->
                BoundCalculatorKey(
                    spec = spec,
                    enabled = true,
                    onSpecClick = onSpecClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (expand) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.height(CalculinoSizes.keyHeight)
                        },
                    ),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSizes.keyGap),
            ) {
                row.forEach { spec ->
                    BoundCalculatorKey(
                        spec = spec,
                        enabled = spec.kind != CalcKeyKind.Equals || canEvaluate,
                        onSpecClick = onSpecClick,
                        modifier = Modifier
                            .weight(spec.span.toFloat())
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoundCalculatorKey(
    spec: CalcKeySpec,
    enabled: Boolean,
    onSpecClick: (CalcKeySpec) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHaptics()
    CalculatorKeyButton(
        spec = spec,
        enabled = enabled,
        onClick = {
            val feedback = if (spec.kind == CalcKeyKind.Equals) {
                HapticFeedbackType.LongPress
            } else {
                HapticFeedbackType.TextHandleMove
            }
            haptic(feedback)
            onSpecClick(spec)
        },
        modifier = modifier,
    )
}

@Composable
private fun digitRow(
    first: Int,
    second: Int,
    third: Int,
    operatorLabel: String,
    operatorDescription: String,
    operatorKind: CalcKeyKind,
): List<CalcKeySpec> = listOf(
    CalcKeySpec(first.toString(), stringResource(R.string.calc_desc_digit, first.toString()), CalcKeyKind.Digit, first.toString()),
    CalcKeySpec(second.toString(), stringResource(R.string.calc_desc_digit, second.toString()), CalcKeyKind.Digit, second.toString()),
    CalcKeySpec(third.toString(), stringResource(R.string.calc_desc_digit, third.toString()), CalcKeyKind.Digit, third.toString()),
    CalcKeySpec(operatorLabel, operatorDescription, operatorKind, operatorLabel),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorFunctionsSheet(
    expression: String,
    onInsert: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = rememberHaptics()
    val trigonometryRows = listOf(
        listOf(
            functionSpec("sin(", R.string.calc_desc_sin),
            functionSpec("cos(", R.string.calc_desc_cos),
            functionSpec("tan(", R.string.calc_desc_tan),
            functionSpec(stringResource(R.string.calc_key_pi), R.string.calc_desc_pi),
        ),
        listOf(
            functionSpec("asin(", R.string.calc_desc_asin),
            functionSpec("acos(", R.string.calc_desc_acos),
            functionSpec("atan(", R.string.calc_desc_atan),
            functionSpec("e", R.string.calc_desc_e),
        ),
    )
    val moreRows = listOf(
        listOf(
            functionSpec("ln(", R.string.calc_desc_ln),
            functionSpec("log(", R.string.calc_desc_log),
            functionSpec("exp(", R.string.calc_desc_exp),
            functionSpec("√(", R.string.calc_desc_sqrt),
        ),
        listOf(
            functionSpec("∛(", R.string.calc_desc_cbrt),
            functionSpec("x²", R.string.calc_desc_square, "^2"),
            functionSpec("!", R.string.calc_desc_factorial),
            functionSpec("(", R.string.calc_desc_open_parenthesis),
            functionSpec(")", R.string.calc_desc_close_parenthesis),
        ),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.textPrimary,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ElasticSheetContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalculinoSpacing.lg)
                    .padding(bottom = CalculinoSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.functions_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.title,
                    )
                    Text(
                        text = stringResource(R.string.functions_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
                DisplayChip(
                    label = stringResource(R.string.functions_done),
                    description = stringResource(R.string.functions_done),
                    containerColor = palette.keyPlus,
                    contentColor = palette.onKeyPlus,
                    onClick = onDismiss,
                )
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = palette.surfaceMuted,
                border = BorderStroke(CalculinoSizes.hairline, palette.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = expression.ifBlank { stringResource(R.string.calculator_zero) },
                    modifier = Modifier.padding(CalculinoSpacing.md),
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.functions_trigonometry),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            trigonometryRows.forEach { row ->
                FunctionRow(
                    specs = row,
                    onInsert = { token ->
                        haptic(HapticFeedbackType.TextHandleMove)
                        onInsert(token)
                    },
                )
            }
            Text(
                text = stringResource(R.string.functions_more),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            moreRows.forEach { row ->
                FunctionRow(
                    specs = row,
                    onInsert = { token ->
                        haptic(HapticFeedbackType.TextHandleMove)
                        onInsert(token)
                    },
                )
            }
            }
        }
    }
}

@Composable
private fun FunctionRow(
    specs: List<CalcKeySpec>,
    onInsert: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CalculinoSizes.scientificKeyHeight),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSizes.keyGap),
    ) {
        specs.forEach { spec ->
            CalculatorKeyButton(
                spec = spec,
                onClick = { spec.token?.let(onInsert) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun functionSpec(
    label: String,
    descriptionRes: Int,
    token: String = label,
): CalcKeySpec = CalcKeySpec(
    label = label,
    description = stringResource(descriptionRes),
    kind = CalcKeyKind.Function,
    token = token,
)

@Composable
private fun errorMessage(error: MathError): String = when (error) {
    MathError.EmptyExpression -> stringResource(R.string.calc_error_empty)
    is MathError.UnexpectedToken -> stringResource(R.string.calc_error_unexpected_token, error.token)
    is MathError.UnexpectedCharacter -> stringResource(R.string.calc_error_unexpected_character, error.character)
    is MathError.MalformedNumber -> stringResource(R.string.calc_error_malformed_number, error.text)
    is MathError.UnknownIdentifier -> stringResource(R.string.calc_error_unknown_identifier, error.name)
    MathError.DivisionByZero -> stringResource(R.string.calc_error_division_by_zero)
    is MathError.DomainError -> stringResource(R.string.calc_error_domain)
    MathError.Overflow -> stringResource(R.string.calc_error_overflow)
    MathError.ResultTooLarge -> stringResource(R.string.calc_error_too_large)
    is MathError.InvalidFactorial -> stringResource(R.string.calc_error_factorial)
}
