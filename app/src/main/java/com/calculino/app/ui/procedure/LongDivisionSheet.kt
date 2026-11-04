package com.calculino.app.ui.procedure

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.calculino.app.R
import com.calculino.app.domain.math.LongDivisionProcedure
import com.calculino.app.domain.math.LongDivisionStep
import com.calculino.app.ui.components.ElasticSheetContent
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.KawaiiPalette
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongDivisionSheet(
    procedure: LongDivisionProcedure,
    onDismiss: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        contentColor = palette.textPrimary,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ElasticSheetContent {
            LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = CalculinoSpacing.lg,
                end = CalculinoSpacing.lg,
                top = CalculinoSpacing.xs,
                bottom = CalculinoSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            item { SheetHeader(procedure = procedure, onDismiss = onDismiss) }
            item { Summary(procedure = procedure) }
            item { VisualSection(procedure = procedure) }
            item { Verification(procedure = procedure) }
            item { StepsHeader() }
            itemsIndexed(
                items = procedure.steps,
                key = { index, _ -> index },
                contentType = { _, _ -> "division_step" },
            ) { index, step ->
                DivisionStepCard(
                    procedure = procedure,
                    step = step,
                    index = index,
                )
            }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    procedure: LongDivisionProcedure,
    onDismiss: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        MushroomIcon(
            capColor = palette.title,
            stemColor = palette.title.copy(alpha = 0.7f),
            dotColor = palette.surface,
            modifier = Modifier.size(CalculinoSizes.iconLarge),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.procedure_title),
                style = MaterialTheme.typography.titleLarge,
                color = palette.title,
            )
            Text(
                text = stringResource(
                    R.string.procedure_subtitle,
                    procedure.dividend.toString(),
                    procedure.divisor.toString(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SheetChip(
            label = stringResource(R.string.procedure_close),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun SheetChip(
    label: String,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val shape = MaterialTheme.shapes.extraLarge
    val haptic = rememberHaptics()
    Box(
        modifier = Modifier
            .heightIn(min = CalculinoSizes.minTouchTarget)
            .clip(shape)
            .background(palette.keyPlus)
            .clickable(role = Role.Button) {
                haptic(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = CalculinoSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.onKeyPlus,
        )
    }
}

@Composable
private fun Summary(procedure: LongDivisionProcedure) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        SummaryCard(
            label = stringResource(R.string.procedure_quotient),
            value = procedure.quotient.toString(),
            containerColor = palette.keyNumber,
            contentColor = palette.onKeyNumber,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = stringResource(R.string.procedure_remainder),
            value = procedure.remainder.toString(),
            containerColor = palette.keyCurrency,
            contentColor = palette.onKeyCurrency,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = CalculinoSpacing.md,
                vertical = CalculinoSpacing.sm,
            ),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VisualSection(procedure: LongDivisionProcedure) {
    val palette = LocalKawaiiPalette.current
    val description = stringResource(
        R.string.procedure_visual_description,
        procedure.dividend.toString(),
        procedure.divisor.toString(),
        procedure.quotient.toString(),
        procedure.remainder.toString(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm)) {
        Text(
            text = stringResource(R.string.procedure_visual_caption),
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = palette.surfaceMuted,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = description },
        ) {
            Box(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(CalculinoSpacing.lg),
            ) {
                DivisionVisual(procedure = procedure)
            }
        }
    }
}

@Composable
private fun DivisionVisual(procedure: LongDivisionProcedure) {
    val palette = LocalKawaiiPalette.current
    val cellWidth = CalculinoSizes.divisionCellWidth
    val rowHeight = CalculinoSizes.divisionRowHeight
    val columns = procedure.dividendDigits.size
    val divisorText = procedure.divisor.toString()
    val leftWidth = cellWidth * max(divisorText.length, 1).toFloat()
    val gridStart = leftWidth + CalculinoSizes.divisionSeparatorWidth
    val gridWidth = cellWidth * columns.toFloat()
    val digitStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace)
    val quotientCells = MutableList<Char?>(columns) { null }
    procedure.quotientDigits.forEach { quotientDigit ->
        if (quotientDigit.position in 0 until columns) {
            quotientCells[quotientDigit.position] = quotientDigit.digit.digitToChar()
        }
    }
    val dividendCells = procedure.dividendDigits.map { it.digitToChar() }
    val broughtDownCells = procedure.steps.mapNotNull { step ->
        if (step.nextDigit != null) step.position + 1 else null
    }.toSet()
    val workRows = buildWorkRows(
        procedure = procedure,
        columns = columns,
        palette = palette,
        subtractColor = MaterialTheme.colorScheme.error,
        bringDownColor = palette.onKeyClear,
    )
    val ruleColor = palette.textSecondary.copy(alpha = 0.6f)

    Column {
        Row(
            modifier = Modifier.height(CalculinoSizes.divisionLabelRowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(gridStart))
            Row(
                modifier = Modifier.width(gridWidth),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
            ) {
                Text(
                    text = stringResource(R.string.procedure_quotient),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(CalculinoSizes.iconSmall),
                )
            }
        }
        Row(modifier = Modifier.height(rowHeight)) {
            Spacer(modifier = Modifier.width(gridStart))
            DivisionCells(
                cells = quotientCells,
                color = palette.title,
                cellWidth = cellWidth,
                rowHeight = rowHeight,
                style = digitStyle,
            )
        }
        Row(
            modifier = Modifier.height(CalculinoSizes.divisionRuleRowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(gridStart))
            Box(
                modifier = Modifier
                    .width(gridWidth)
                    .height(CalculinoSizes.divisionRuleHeight)
                    .background(ruleColor),
            )
        }
        Row(
            modifier = Modifier.height(rowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = divisorText,
                modifier = Modifier
                    .width(leftWidth)
                    .padding(end = CalculinoSpacing.xs),
                style = digitStyle,
                color = palette.textPrimary,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
            Box(
                modifier = Modifier
                    .width(CalculinoSizes.divisionSeparatorWidth)
                    .height(rowHeight)
                    .background(palette.textPrimary),
            )
            DivisionCells(
                cells = dividendCells,
                color = palette.textPrimary,
                cellWidth = cellWidth,
                rowHeight = rowHeight,
                style = digitStyle,
                struckCells = broughtDownCells,
            )
        }
        workRows.forEach { row ->
            if (row.ruleStart != null && row.ruleEnd != null) {
                val span = row.ruleEnd - row.ruleStart + 1
                Row(
                    modifier = Modifier.height(CalculinoSizes.divisionRuleRowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(
                        modifier = Modifier.width(
                            gridStart + cellWidth * row.ruleStart.toFloat(),
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .width(cellWidth * span.toFloat())
                            .height(CalculinoSizes.divisionRuleHeight)
                            .background(row.color),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.height(rowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(leftWidth)
                            .padding(end = CalculinoSpacing.xs),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        when (row.mark) {
                            DivisionRowMark.Subtract -> Text(
                                text = "−",
                                style = MaterialTheme.typography.titleMedium,
                                color = row.color,
                            )
                            DivisionRowMark.BringDown -> Icon(
                                imageVector = Icons.Rounded.ArrowDownward,
                                contentDescription = null,
                                tint = row.color,
                                modifier = Modifier.size(CalculinoSizes.iconSmall),
                            )
                            DivisionRowMark.None -> Unit
                        }
                    }
                    Spacer(modifier = Modifier.width(CalculinoSizes.divisionSeparatorWidth))
                    DivisionCells(
                        cells = row.cells.orEmpty(),
                        color = row.color,
                        cellWidth = cellWidth,
                        rowHeight = rowHeight,
                        style = digitStyle,
                    )
                }
            }
        }
        procedure.steps.lastOrNull()?.let { lastStep ->
            val remainderStart = max(
                0,
                lastStep.position - lastStep.remainder.toString().length + 1,
            )
            Row(
                modifier = Modifier.height(CalculinoSizes.divisionLabelRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(
                    modifier = Modifier.width(
                        gridStart + cellWidth * remainderStart.toFloat(),
                    ),
                )
                Text(
                    text = "↑ ${stringResource(R.string.procedure_remainder)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun DivisionCells(
    cells: List<Char?>,
    color: Color,
    cellWidth: Dp,
    rowHeight: Dp,
    style: TextStyle,
    struckCells: Set<Int> = emptySet(),
) {
    Row {
        cells.forEachIndexed { index, character ->
            Box(
                modifier = Modifier
                    .width(cellWidth)
                    .height(rowHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (character != null) {
                    Text(
                        text = character.toString(),
                        style = style,
                        color = color,
                        maxLines = 1,
                        textDecoration = if (index in struckCells) {
                            TextDecoration.LineThrough
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

private enum class DivisionRowMark {
    None,
    Subtract,
    BringDown,
}

private data class DivisionWorkRow(
    val cells: List<Char?>? = null,
    val color: Color,
    val ruleStart: Int? = null,
    val ruleEnd: Int? = null,
    val mark: DivisionRowMark = DivisionRowMark.None,
)

private fun buildWorkRows(
    procedure: LongDivisionProcedure,
    columns: Int,
    palette: KawaiiPalette,
    subtractColor: Color,
    bringDownColor: Color,
): List<DivisionWorkRow> {
    val rows = mutableListOf<DivisionWorkRow>()
    procedure.steps.forEachIndexed { index, step ->
        if (step.quotientDigit > 0) {
            rows += DivisionWorkRow(
                cells = placeDigits(step.product.toString(), step.position, columns),
                color = subtractColor,
                mark = DivisionRowMark.Subtract,
            )
            val ruleStart = max(0, step.position - step.currentValue.toString().length + 1)
            rows += DivisionWorkRow(
                color = subtractColor.copy(alpha = 0.7f),
                ruleStart = ruleStart,
                ruleEnd = step.position,
            )
        }
        if (index < procedure.steps.lastIndex) {
            val nextPartial = step.nextPartial
            if (nextPartial != null) {
                rows += DivisionWorkRow(
                    cells = placeDigits(nextPartial.toString(), step.position + 1, columns),
                    color = bringDownColor,
                    mark = DivisionRowMark.BringDown,
                )
            }
        } else {
            rows += DivisionWorkRow(
                cells = placeDigits(step.remainder.toString(), step.position, columns),
                color = palette.textPrimary,
            )
        }
    }
    return rows
}

private fun placeDigits(
    value: String,
    endPosition: Int,
    columns: Int,
): List<Char?> {
    val cells = MutableList<Char?>(columns) { null }
    val start = endPosition - value.length + 1
    value.forEachIndexed { index, character ->
        val column = start + index
        if (column in 0 until columns) cells[column] = character
    }
    return cells
}

@Composable
private fun Verification(procedure: LongDivisionProcedure) {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = if (procedure.isExact) palette.keyClear else palette.keyPercent,
        ) {
            Text(
                text = stringResource(
                    if (procedure.isExact) R.string.procedure_exact else R.string.procedure_inexact,
                ),
                modifier = Modifier.padding(
                    horizontal = CalculinoSpacing.sm,
                    vertical = CalculinoSpacing.xxs,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (procedure.isExact) palette.onKeyClear else palette.onKeyPercent,
            )
        }
        Text(
            text = stringResource(
                R.string.procedure_verification,
                procedure.divisor.toString(),
                procedure.quotient.toString(),
                procedure.remainder.toString(),
                procedure.dividend.toString(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun StepsHeader() {
    val palette = LocalKawaiiPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs)) {
        Text(
            text = stringResource(R.string.procedure_steps_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
        )
        Text(
            text = stringResource(R.string.procedure_steps_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun DivisionStepCard(
    procedure: LongDivisionProcedure,
    step: LongDivisionStep,
    index: Int,
) {
    val palette = LocalKawaiiPalette.current
    val shape = MaterialTheme.shapes.large
    val haptic = rememberHaptics()
    var expanded by rememberSaveable { mutableStateOf(index == 0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.surfaceMuted)
            .border(CalculinoSizes.hairline, palette.border, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) {
                    haptic(HapticFeedbackType.TextHandleMove)
                    expanded = !expanded
                }
                .padding(CalculinoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Surface(
                shape = CircleShape,
                color = palette.keyMultiply,
                modifier = Modifier.size(CalculinoSizes.stepNumberSize),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.onKeyMultiply,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.procedure_step_number, index + 1),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                )
                Text(
                    text = stringResource(
                        R.string.procedure_step_division,
                        step.currentValue.toString(),
                        procedure.divisor.toString(),
                        step.quotientDigit.toString(),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = stringResource(
                    if (expanded) R.string.procedure_collapse_step else R.string.procedure_expand_step,
                ),
                tint = palette.icon,
                modifier = Modifier
                    .size(CalculinoSizes.iconMedium)
                    .rotate(if (expanded) 180f else 0f),
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier.padding(
                    start = CalculinoSpacing.md,
                    end = CalculinoSpacing.md,
                    bottom = CalculinoSpacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                if (step.quotientDigit > 0) {
                    StepLine(
                        text = stringResource(
                            R.string.procedure_step_multiply,
                            step.quotientDigit.toString(),
                            procedure.divisor.toString(),
                            step.product.toString(),
                        ),
                    )
                    StepLine(
                        text = stringResource(
                            R.string.procedure_step_subtract,
                            step.currentValue.toString(),
                            step.product.toString(),
                            step.remainder.toString(),
                        ),
                    )
                } else {
                    StepLine(
                        text = stringResource(
                            R.string.procedure_step_no_fit,
                            step.currentValue.toString(),
                            procedure.divisor.toString(),
                        ),
                    )
                }
                val nextDigit = step.nextDigit
                val nextPartial = step.nextPartial
                if (nextDigit != null && nextPartial != null) {
                    StepLine(
                        text = stringResource(
                            R.string.procedure_step_bring_down,
                            nextDigit.toString(),
                            nextPartial.toString(),
                        ),
                    )
                }
                if (index == procedure.steps.lastIndex) {
                    StepLine(
                        text = if (procedure.isExact) {
                            stringResource(R.string.procedure_step_exact)
                        } else {
                            stringResource(
                                R.string.procedure_step_remainder,
                                procedure.remainder.toString(),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StepLine(text: String) {
    val palette = LocalKawaiiPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(CalculinoSizes.stepBulletSize)
                .background(palette.title.copy(alpha = 0.7f), CircleShape),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}
