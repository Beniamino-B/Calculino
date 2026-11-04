package com.calculino.app.ui.engineering

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.calculino.app.R
import com.calculino.app.domain.engineering.ComplexityReference
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun ComplexityRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComplexityScreen(onBack = onBack, modifier = modifier)
}

@Composable
fun ComplexityScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_complexity_title),
        subtitle = stringResource(R.string.tool_complexity_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        ComplexityReference.entries.forEach { entry ->
            EngineeringCard(title = entry.name) {
                Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
                    ComplexityRow(
                        label = stringResource(R.string.complexity_best),
                        value = entry.best,
                    )
                    ComplexityRow(
                        label = stringResource(R.string.complexity_average),
                        value = entry.average,
                    )
                    ComplexityRow(
                        label = stringResource(R.string.complexity_worst),
                        value = entry.worst,
                    )
                    ComplexityRow(
                        label = stringResource(R.string.complexity_space),
                        value = entry.space,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplexityRow(
    label: String,
    value: String,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = palette.textPrimary,
        )
    }
}
