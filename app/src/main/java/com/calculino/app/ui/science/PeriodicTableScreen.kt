package com.calculino.app.ui.science

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.calculino.app.R
import com.calculino.app.domain.periodic.ChemicalElement
import com.calculino.app.domain.periodic.ElementCategory
import com.calculino.app.domain.periodic.ElementPhase
import com.calculino.app.domain.periodic.PeriodicTable
import com.calculino.app.ui.components.ElasticSheetContent
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import java.math.BigDecimal
import java.text.Normalizer

private val accentMarks = Regex("\\p{Mn}+")

@Composable
fun PeriodicTableRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PeriodicTableScreen(onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicTableScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    val positions = remember { PeriodicTable.elements.associateBy { element -> element.xpos to element.ypos } }
    val filtered = remember(query) { searchElements(query) }
    val selectedElement = selectedNumber?.let { PeriodicTable.byNumber[it] }

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
                title = stringResource(R.string.periodic_title),
                subtitle = stringResource(R.string.periodic_subtitle),
                onBack = onBack,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CalculinoSpacing.sm),
                placeholder = { Text(stringResource(R.string.periodic_search_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = palette.icon,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.periodic_clear_search),
                                tint = palette.icon,
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
            )
            if (query.isBlank()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = CalculinoSpacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
                ) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = palette.surface,
                            border = BorderStroke(CalculinoSizes.hairline, palette.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .kawaiiCard(),
                        ) {
                            PeriodicTableGrid(
                                positions = positions,
                                onElementClick = { element ->
                                    haptic(HapticFeedbackType.TextHandleMove)
                                    selectedNumber = element.number
                                },
                            )
                        }
                    }
                    item { CategoryLegend() }
                    item {
                        Text(
                            text = stringResource(R.string.periodic_table_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                    item { SourceNote() }
                    item { Spacer(modifier = Modifier.height(CalculinoSpacing.xxl)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = CalculinoSpacing.xxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    if (filtered.isEmpty()) {
                        item { NoResults() }
                    } else {
                        items(
                            items = filtered,
                            key = { element -> element.number },
                            contentType = { "element" },
                        ) { element ->
                            SearchResultCard(
                                element = element,
                                onClick = {
                                    haptic(HapticFeedbackType.TextHandleMove)
                                    selectedNumber = element.number
                                },
                            )
                        }
                    }
                    item { SourceNote() }
                }
            }
        }
    }

    if (selectedElement != null) {
        ElementDetailSheet(
            element = selectedElement,
            onDismiss = { selectedNumber = null },
        )
    }
}

@Composable
private fun PeriodicTableGrid(
    positions: Map<Pair<Int, Int>, ChemicalElement>,
    onElementClick: (ChemicalElement) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val gap = CalculinoSpacing.xxs
    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(CalculinoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            for (column in 1..18) {
                Box(
                    modifier = Modifier
                        .width(CalculinoSizes.elementCellWidth)
                        .height(CalculinoSizes.elementCellHeight / 3f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = column.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(CalculinoSpacing.xxs))
        for (row in 1..10) {
            if (row == 8) {
                Spacer(modifier = Modifier.height(CalculinoSpacing.sm))
                continue
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                var column = 1
                while (column <= 18) {
                    val seriesLabel = when {
                        row == 9 && column == 1 -> R.string.category_lanthanide
                        row == 10 && column == 1 -> R.string.category_actinide
                        else -> null
                    }
                    if (seriesLabel != null) {
                        Box(
                            modifier = Modifier
                                .width(CalculinoSizes.elementCellWidth * 2f + gap)
                                .height(CalculinoSizes.elementCellHeight)
                                .clip(RoundedCornerShape(CalculinoSizes.elementCellCorner))
                                .background(palette.surfaceMuted)
                                .padding(horizontal = CalculinoSpacing.sm),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = stringResource(seriesLabel),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                                maxLines = 1,
                            )
                        }
                        column += 2
                        continue
                    }
                    val element = positions[column to row]
                    if (element == null) {
                        Spacer(
                            modifier = Modifier
                                .width(CalculinoSizes.elementCellWidth)
                                .height(CalculinoSizes.elementCellHeight),
                        )
                    } else {
                        ElementCell(element = element, onClick = { onElementClick(element) })
                    }
                    column++
                }
            }
        }
    }
}

@Composable
private fun ElementCell(
    element: ChemicalElement,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val container = elementCategoryContainer(element.category)
    val content = elementCategoryContent(element.category)
    val shape = RoundedCornerShape(CalculinoSizes.elementCellCorner)
    val description = stringResource(
        R.string.periodic_element_description,
        element.name,
        element.symbol,
        element.number,
    )
    Box(
        modifier = Modifier
            .width(CalculinoSizes.elementCellWidth)
            .height(CalculinoSizes.elementCellHeight)
            .clip(shape)
            .background(container)
            .border(CalculinoSizes.hairline, palette.border.copy(alpha = 0.6f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = element.number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.72f),
            )
            Text(
                text = element.symbol,
                style = MaterialTheme.typography.titleSmall,
                color = content,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategoryLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ElementCategory.entries.forEach { category ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(CalculinoSizes.elementDotSize)
                        .background(elementCategoryContainer(category), CircleShape),
                )
                Text(
                    text = elementCategoryLabel(category),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalKawaiiPalette.current.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    element: ChemicalElement,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val container = elementCategoryContainer(element.category)
    val content = elementCategoryContent(element.category)
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = container,
                modifier = Modifier.size(CalculinoSizes.minTouchTarget),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = element.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        color = content,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = element.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.periodic_search_result_meta,
                        element.number,
                        formatAtomicMass(element),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Text(
                    text = elementCategoryLabel(element.category),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NoResults() {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CalculinoSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
    ) {
        Text(
            text = stringResource(R.string.periodic_no_results_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.title,
        )
        Text(
            text = stringResource(R.string.periodic_no_results_text),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun SourceNote() {
    Text(
        text = stringResource(R.string.periodic_source_note),
        style = MaterialTheme.typography.bodySmall,
        color = LocalKawaiiPalette.current.textSecondary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElementDetailSheet(
    element: ChemicalElement,
    onDismiss: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val container = elementCategoryContainer(element.category)
    val content = elementCategoryContent(element.category)
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
                    .verticalScroll(rememberScrollState())
                .padding(horizontal = CalculinoSpacing.lg)
                .padding(bottom = CalculinoSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = container,
                    modifier = Modifier.size(CalculinoSizes.elementSymbolBox),
                ) {
                    Column(
                        modifier = Modifier.padding(CalculinoSpacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = element.number.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = content.copy(alpha = 0.75f),
                        )
                        Text(
                            text = element.symbol,
                            style = MaterialTheme.typography.headlineLarge,
                            color = content,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = element.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.title,
                    )
                    Text(
                        text = elementCategoryLabel(element.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.procedure_close))
                }
            }
            DetailRow(
                label = stringResource(R.string.periodic_atomic_number),
                value = element.number.toString(),
            )
            DetailRow(
                label = stringResource(R.string.periodic_atomic_mass),
                value = "${formatAtomicMass(element)} ${stringResource(R.string.periodic_atomic_mass_unit)}",
            )
            DetailRow(
                label = stringResource(R.string.periodic_period),
                value = element.period.toString(),
            )
            DetailRow(
                label = stringResource(R.string.periodic_group),
                value = element.group.toString(),
            )
            DetailRow(
                label = stringResource(R.string.periodic_phase),
                value = phaseLabel(element.phase),
            )
            DetailRow(
                label = stringResource(R.string.periodic_electron_configuration),
                value = element.electronConfiguration,
                monospace = true,
            )
            if (element.massIsIsotopeNumber) {
                Text(
                    text = stringResource(R.string.periodic_mass_number_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
            if (element.funFact.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kawaiiCard(MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .background(palette.surfaceMuted)
                        .border(
                            CalculinoSizes.hairline,
                            palette.border,
                            MaterialTheme.shapes.medium,
                        )
                        .padding(CalculinoSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
                ) {
                    Text(
                        text = stringResource(R.string.periodic_fun_fact),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary,
                    )
                    Text(
                        text = element.funFact,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary,
                    )
                }
            }
            Text(
                text = stringResource(R.string.periodic_source_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    monospace: Boolean = false,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CalculinoSizes.minTouchTarget)
            .clip(MaterialTheme.shapes.small)
            .background(palette.surfaceMuted)
            .padding(horizontal = CalculinoSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.4f),
            style = if (monospace) {
                MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = palette.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
private fun phaseLabel(phase: ElementPhase): String = stringResource(
    when (phase) {
        ElementPhase.Solid -> R.string.phase_solid
        ElementPhase.Liquid -> R.string.phase_liquid
        ElementPhase.Gas -> R.string.phase_gas
    },
)

private fun searchElements(query: String): List<ChemicalElement> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()
    if (trimmed.all { it.isDigit() }) {
        return PeriodicTable.elements.filter { it.number.toString() == trimmed }
    }
    val normalized = normalize(trimmed)
    val lowered = trimmed.lowercase()
    return PeriodicTable.elements
        .filter { element ->
            element.symbol.lowercase().contains(lowered) ||
                normalize(element.name).contains(normalized)
        }
        .sortedWith(
            compareBy(
                { it.symbol.lowercase() != lowered },
                { !it.symbol.lowercase().startsWith(lowered) },
                { !normalize(it.name).startsWith(normalized) },
                { it.number },
            ),
        )
}

private fun normalize(value: String): String =
    Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
        .replace(accentMarks, "")

private fun formatAtomicMass(element: ChemicalElement): String =
    BigDecimal.valueOf(element.atomicMass).stripTrailingZeros().toPlainString()
