package com.calculino.app.ui.chemistry

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.chemistry.ElementContribution
import com.calculino.app.domain.chemistry.MolarMassError
import com.calculino.app.domain.chemistry.MolarMassOutcome
import com.calculino.app.domain.chemistry.MolarMassResult
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.science.elementCategoryContainer
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

private val exampleFormulas = listOf(
    "H2O",
    "CO2",
    "Ca(OH)2",
    "C6H12O6",
    "CuSO4·5H2O",
    "(NH4)2SO4",
)

@Composable
fun MolarMassRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MolarMassViewModel = viewModel(
        initializer = { MolarMassViewModel(createSavedStateHandle()) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MolarMassScreen(
        state = state,
        onFormulaChange = viewModel::onFormulaChange,
        onClear = viewModel::clear,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun MolarMassScreen(
    state: MolarMassUiState,
    onFormulaChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
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
                title = stringResource(R.string.molar_title),
                subtitle = stringResource(R.string.molar_subtitle),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
                    .padding(vertical = CalculinoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                OutlinedTextField(
                    value = state.formula,
                    onValueChange = onFormulaChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.molar_formula_label)) },
                    placeholder = { Text(stringResource(R.string.molar_formula_placeholder)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    isError = state.outcome is MolarMassOutcome.Failure,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Functions,
                            contentDescription = null,
                            tint = palette.icon,
                        )
                    },
                    trailingIcon = {
                        if (state.formula.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.molar_clear_formula),
                                    tint = palette.icon,
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                )
                Text(
                    text = stringResource(R.string.molar_examples),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    exampleFormulas.forEach { formula ->
                        ExampleChip(
                            formula = formula,
                            onClick = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onFormulaChange(formula)
                            },
                        )
                    }
                }
                when (val outcome = state.outcome) {
                    null -> EmptyCard()
                    is MolarMassOutcome.Failure -> ErrorCard(outcome.error)
                    is MolarMassOutcome.Success -> ResultContent(outcome.result)
                }
                Text(
                    text = stringResource(R.string.molar_source_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
            }
        }
    }
}

@Composable
private fun ExampleChip(
    formula: String,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier
            .heightIn(min = CalculinoSizes.minTouchTarget)
            .clip(shape)
            .background(palette.surfaceMuted)
            .border(CalculinoSizes.hairline, palette.border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CalculinoSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formula,
            style = MaterialTheme.typography.labelMedium,
            color = palette.textPrimary,
        )
    }
}

@Composable
private fun EmptyCard() {
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
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.molar_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
            Text(
                text = stringResource(R.string.molar_empty_text),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun ErrorCard(error: MolarMassError) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = molarErrorMessage(error),
            modifier = Modifier.padding(CalculinoSpacing.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun ResultContent(result: MolarMassResult) {
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
                text = stringResource(R.string.molar_total_label),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                Text(
                    text = result.molarMassDisplay,
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.molar_mass_unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(bottom = CalculinoSpacing.xs),
                )
            }
            Text(
                text = pluralStringResource(
                    R.plurals.molar_atom_count,
                    result.totalAtoms,
                    result.totalAtoms,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
            )
            Text(
                text = stringResource(R.string.molar_composition),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
            result.composition.forEach { contribution ->
                ContributionRow(contribution)
            }
        }
    }
}

@Composable
private fun ContributionRow(contribution: ElementContribution) {
    val palette = LocalKawaiiPalette.current
    val categoryColor = elementCategoryContainer(contribution.element.category)
    val fraction = (contribution.percentage.toFloat() / 100f).coerceIn(0f, 1f)
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surfaceMuted,
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(CalculinoSizes.elementDotSize)
                        .background(categoryColor, CircleShape),
                )
                Spacer(modifier = Modifier.size(CalculinoSpacing.sm))
                Text(
                    text = contribution.element.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
                Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
                Text(
                    text = contribution.element.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.molar_atoms_factor, contribution.atomCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textPrimary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.molar_contribution_mass, contribution.massDisplay),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.molar_percentage, contribution.percentageDisplay),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textPrimary,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CalculinoSizes.compositionBarHeight)
                    .clip(CircleShape)
                    .background(palette.surfaceStrong),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(categoryColor),
                )
            }
        }
    }
}

@Composable
private fun molarErrorMessage(error: MolarMassError): String = when (error) {
    MolarMassError.EmptyFormula -> stringResource(R.string.molar_error_empty)
    is MolarMassError.UnknownElement -> stringResource(R.string.molar_error_unknown, error.symbol)
    is MolarMassError.UnexpectedCharacter -> stringResource(R.string.molar_error_character, error.character)
    is MolarMassError.UnbalancedParenthesis -> stringResource(R.string.molar_error_parentheses)
    is MolarMassError.EmptyGroup -> stringResource(R.string.molar_error_empty_group)
    is MolarMassError.InvalidCount -> stringResource(R.string.molar_error_count)
    MolarMassError.TooLarge -> stringResource(R.string.molar_error_too_large)
}
