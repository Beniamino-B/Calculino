package com.calculino.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.calculino.app.R
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun ExactnessBadge(
    isExact: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isExact) palette.keyClear else palette.keyCurrency,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(
                if (isExact) R.string.calculator_result_exact else R.string.calculator_result_approximate,
            ),
            modifier = Modifier.padding(
                horizontal = CalculinoSpacing.sm,
                vertical = CalculinoSpacing.xxs,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (isExact) palette.onKeyClear else palette.onKeyCurrency,
        )
    }
}
