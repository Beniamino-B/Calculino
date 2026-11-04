package com.calculino.app.ui.history

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.math.MathStepBuilder
import com.calculino.app.domain.model.HistoryEntry
import com.calculino.app.ui.components.ExactnessBadge
import com.calculino.app.ui.components.PawIcon
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    historyRepository: HistoryRepository,
    onDismiss: () -> Unit,
) {
    val viewModel: HistoryViewModel = viewModel(
        key = "calculator-history",
        initializer = { HistoryViewModel(historyRepository) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = rememberHaptics()
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    val palette = LocalKawaiiPalette.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = CalculinoSpacing.lg,
                    end = CalculinoSpacing.lg,
                    bottom = CalculinoSpacing.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.title,
                    )
                    Text(
                        text = stringResource(R.string.history_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
                if (state.entries.isNotEmpty() && !state.isLoading) {
                    IconButton(
                        onClick = {
                            haptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            showClearDialog = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = stringResource(R.string.history_clear_description),
                            tint = palette.textSecondary,
                        )
                    }
                }
            }
            when {
                state.isLoading -> HistoryLoading()
                state.hasError -> HistoryError(onRetry = viewModel::retry)
                state.entries.isEmpty() -> HistoryEmpty()
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = CalculinoSizes.historySheetMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                ) {
                    items(
                        items = state.entries,
                        key = { entry -> entry.id },
                        contentType = { "history_sheet_entry" },
                    ) { entry ->
                        HistorySheetCard(
                            entry = entry,
                            onDelete = {
                                haptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                viewModel.delete(entry.id)
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(CalculinoSpacing.sm)) }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.history_clear_dialog_title)) },
            text = { Text(stringResource(R.string.history_clear_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clear()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.history_clear_confirm),
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
private fun HistorySheetCard(
    entry: HistoryEntry,
    onDelete: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    val steps = remember(entry.id, entry.expression, entry.result) {
        MathStepBuilder.build(
            expression = entry.expression,
            fallbackResult = entry.result,
            fallbackApproximation = entry.approximation,
            angleMode = entry.angleMode,
        ).steps
    }
    val timestamp = remember(entry.createdAt) {
        DateUtils.getRelativeTimeSpanString(
            entry.createdAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = entry.expression,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.history_delete_entry),
                        tint = palette.textSecondary,
                        modifier = Modifier.size(CalculinoSizes.iconSmall),
                    )
                }
            }
            Text(
                text = "= ${entry.result}",
                style = MaterialTheme.typography.titleMedium,
                color = palette.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            entry.approximation?.let { approximation ->
                Text(
                    text = stringResource(R.string.calculator_approx_detail, approximation),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                ExactnessBadge(isExact = entry.isExact)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                )
            }
            TextButton(
                onClick = { expanded = !expanded },
                shape = PillShape,
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(CalculinoSizes.iconSmall),
                )
                Text(
                    text = stringResource(
                        if (expanded) R.string.history_hide_steps else R.string.history_show_steps,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
                    steps.forEachIndexed { index, step ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.expression,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textPrimary,
                                )
                                Text(
                                    text = step.result,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryLoading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(CalculinoSizes.iconMedium),
            strokeWidth = CalculinoSizes.focusBorder,
        )
        Text(
            text = stringResource(R.string.history_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalKawaiiPalette.current.textSecondary,
        )
    }
}

@Composable
private fun HistoryEmpty() {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        PawIcon(
            color = palette.decorationAccent.copy(alpha = 0.8f),
            modifier = Modifier.size(CalculinoSizes.decorationLarge),
        )
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.title,
        )
        Text(
            text = stringResource(R.string.history_empty_text),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun HistoryError(onRetry: () -> Unit) {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.history_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.title,
        )
        Text(
            text = stringResource(R.string.history_error_text),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
        Button(onClick = onRetry, shape = PillShape) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(CalculinoSizes.iconSmall),
            )
            Spacer(modifier = Modifier.size(CalculinoSpacing.xs))
            Text(stringResource(R.string.action_retry))
        }
    }
}
