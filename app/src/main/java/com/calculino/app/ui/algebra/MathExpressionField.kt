package com.calculino.app.ui.algebra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import com.calculino.app.R
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun DottedMathField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val label = stringResource(R.string.algebra_input_label)
    val placeholder = stringResource(R.string.algebra_input_hint)
    val description = stringResource(R.string.algebra_input_description)
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CalculinoSizes.minTouchTarget)
                .drawBehind {
                    drawLine(
                        color = palette.textSecondary,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = CalculinoSizes.focusBorder.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f)),
                    )
                }
                .semantics { contentDescription = description },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = palette.textPrimary,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(palette.title),
            maxLines = 5,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CalculinoSpacing.md),
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}
