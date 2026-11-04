package com.calculino.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun Modifier.kawaiiCard(shape: Shape = MaterialTheme.shapes.extraLarge): Modifier {
    val palette = LocalKawaiiPalette.current
    return shadow(
        elevation = CalculinoSizes.cardElevation,
        shape = shape,
        clip = false,
        ambientColor = palette.shadow,
        spotColor = palette.shadow,
    )
}
