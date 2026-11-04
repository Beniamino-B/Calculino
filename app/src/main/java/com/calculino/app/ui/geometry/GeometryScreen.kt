package com.calculino.app.ui.geometry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.geometry.GeometryMeasurements
import com.calculino.app.domain.geometry.GeometryShape
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min

@Composable
fun GeometryRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GeometryViewModel = viewModel(
        initializer = { GeometryViewModel(createSavedStateHandle()) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    GeometryScreen(
        state = state,
        onShapeChange = viewModel::onShapeChange,
        onCircleRadiusChange = viewModel::onCircleRadiusChange,
        onRectangleWidthChange = viewModel::onRectangleWidthChange,
        onRectangleHeightChange = viewModel::onRectangleHeightChange,
        onTriangleBaseChange = viewModel::onTriangleBaseChange,
        onTriangleHeightChange = viewModel::onTriangleHeightChange,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun GeometryScreen(
    state: GeometryUiState,
    onShapeChange: (GeometryShape) -> Unit,
    onCircleRadiusChange: (Float) -> Unit,
    onRectangleWidthChange: (Float) -> Unit,
    onRectangleHeightChange: (Float) -> Unit,
    onTriangleBaseChange: (Float) -> Unit,
    onTriangleHeightChange: (Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val scroll = rememberScrollState()

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
            ScienceToolHeader(
                title = stringResource(R.string.geometry_title),
                subtitle = stringResource(R.string.geometry_subtitle),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
                    .padding(vertical = CalculinoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                ShapeSelector(
                    selected = state.shape,
                    onSelect = onShapeChange,
                )
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = palette.surfaceMuted,
                    border = BorderStroke(CalculinoSizes.hairline, palette.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .kawaiiCard(),
                ) {
                    ShapeDiagram(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CalculinoSizes.geometryDiagramHeight)
                            .padding(CalculinoSpacing.lg),
                    )
                }
                when (state.shape) {
                    GeometryShape.Circle -> {
                        DimensionSlider(
                            label = stringResource(R.string.geometry_radius),
                            value = state.circleRadius,
                            onValueChange = onCircleRadiusChange,
                        )
                    }
                    GeometryShape.Rectangle -> {
                        DimensionSlider(
                            label = stringResource(R.string.geometry_width),
                            value = state.rectangleWidth,
                            onValueChange = onRectangleWidthChange,
                        )
                        DimensionSlider(
                            label = stringResource(R.string.geometry_height),
                            value = state.rectangleHeight,
                            onValueChange = onRectangleHeightChange,
                        )
                    }
                    GeometryShape.RightTriangle -> {
                        DimensionSlider(
                            label = stringResource(R.string.geometry_base),
                            value = state.triangleBase,
                            onValueChange = onTriangleBaseChange,
                        )
                        DimensionSlider(
                            label = stringResource(R.string.geometry_height),
                            value = state.triangleHeight,
                            onValueChange = onTriangleHeightChange,
                        )
                    }
                }
                MeasurementCards(measurements = state.measurements)
                Text(
                    text = stringResource(R.string.geometry_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
            }
        }
    }
}

@Composable
private fun ShapeSelector(
    selected: GeometryShape,
    onSelect: (GeometryShape) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        GeometryShape.entries.forEach { shape ->
            val isSelected = shape == selected
            val shapeStyle = MaterialTheme.shapes.large
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(CalculinoSizes.minTouchTarget)
                    .clip(shapeStyle)
                    .background(if (isSelected) palette.keyMultiply else palette.surfaceMuted)
                    .border(
                        width = if (isSelected) CalculinoSizes.focusBorder else CalculinoSizes.hairline,
                        color = if (isSelected) palette.focusRing else palette.border,
                        shape = shapeStyle,
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = {
                            haptic(HapticFeedbackType.TextHandleMove)
                            onSelect(shape)
                        },
                    )
                    .padding(horizontal = CalculinoSpacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = shapeLabel(shape),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) palette.onKeyMultiply else palette.textSecondary,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun DimensionSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            Text(
                text = stringResource(R.string.geometry_value_cm, formatOneDecimal(value)),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.5f..10f,
            colors = SliderDefaults.colors(
                thumbColor = palette.title,
                activeTrackColor = palette.keyMultiply,
                inactiveTrackColor = palette.surfaceStrong,
            ),
        )
    }
}

@Composable
private fun MeasurementCards(measurements: GeometryMeasurements) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        MeasurementCard(
            label = stringResource(R.string.geometry_area),
            value = formatTwoDecimals(measurements.area),
            unit = stringResource(R.string.unit_square_centimeter),
            containerColor = palette.keyNumber,
            contentColor = palette.onKeyNumber,
            modifier = Modifier.weight(1f),
        )
        MeasurementCard(
            label = stringResource(R.string.geometry_perimeter),
            value = formatTwoDecimals(measurements.perimeter),
            unit = stringResource(R.string.unit_centimeter),
            containerColor = palette.keyCurrency,
            contentColor = palette.onKeyCurrency,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MeasurementCard(
    label: String,
    value: String,
    unit: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        modifier = modifier.kawaiiCard(MaterialTheme.shapes.large),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor,
                    maxLines = 1,
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun ShapeDiagram(
    state: GeometryUiState,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val fillColor = palette.keyMultiply.copy(alpha = 0.45f)
    val strokeColor = palette.title
    val guideColor = palette.border
    Canvas(modifier) {
        val padding = size.minDimension * 0.12f
        val availableWidth = size.width - padding * 2
        val availableHeight = size.height - padding * 2
        val strokeWidth = CalculinoSizes.geometryStroke.toPx()
        when (state.shape) {
            GeometryShape.Circle -> {
                val radius = min(availableWidth, availableHeight) / 2f *
                    (state.circleRadius / 10f).coerceIn(0.05f, 1f)
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = fillColor, radius = radius, center = center)
                drawCircle(
                    color = strokeColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = guideColor,
                    start = center,
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = strokeWidth / 2f,
                )
            }
            GeometryShape.Rectangle -> {
                val aspect = state.rectangleWidth / state.rectangleHeight
                val width: Float
                val height: Float
                if (availableWidth / availableHeight > aspect) {
                    height = availableHeight
                    width = height * aspect
                } else {
                    width = availableWidth
                    height = width / aspect
                }
                val topLeft = Offset(
                    (size.width - width) / 2f,
                    (size.height - height) / 2f,
                )
                drawRect(
                    color = fillColor,
                    topLeft = topLeft,
                    size = Size(width, height),
                )
                drawRect(
                    color = strokeColor,
                    topLeft = topLeft,
                    size = Size(width, height),
                    style = Stroke(width = strokeWidth),
                )
            }
            GeometryShape.RightTriangle -> {
                val aspect = state.triangleBase / state.triangleHeight
                val width: Float
                val height: Float
                if (availableWidth / availableHeight > aspect) {
                    height = availableHeight
                    width = height * aspect
                } else {
                    width = availableWidth
                    height = width / aspect
                }
                val left = (size.width - width) / 2f
                val top = (size.height - height) / 2f
                val path = Path().apply {
                    moveTo(left, top)
                    lineTo(left, top + height)
                    lineTo(left + width, top + height)
                    close()
                }
                drawPath(path = path, color = fillColor)
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(width = strokeWidth),
                )
                val marker = height.coerceAtMost(width) * 0.12f
                drawRect(
                    color = guideColor,
                    topLeft = Offset(left, top + height - marker),
                    size = Size(marker, marker),
                    style = Stroke(width = strokeWidth / 2f),
                )
            }
        }
    }
}

@Composable
private fun shapeLabel(shape: GeometryShape): String = stringResource(
    when (shape) {
        GeometryShape.Circle -> R.string.geometry_circle
        GeometryShape.Rectangle -> R.string.geometry_rectangle
        GeometryShape.RightTriangle -> R.string.geometry_right_triangle
    },
)

private fun formatOneDecimal(value: Float): String =
    BigDecimal.valueOf(value.toDouble())
        .setScale(1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()

private fun formatTwoDecimals(value: Double): String =
    BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
