package com.calculino.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun KawaiiBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalKawaiiPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(palette.background, palette.backgroundEnd),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.decorationAccent.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.06f),
                        radius = size.minDimension * 0.85f,
                    ),
                    radius = size.minDimension * 0.85f,
                    center = Offset(size.width * 0.12f, size.height * 0.06f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.decoration.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.92f, size.height * 0.82f),
                        radius = size.minDimension * 0.75f,
                    ),
                    radius = size.minDimension * 0.75f,
                    center = Offset(size.width * 0.92f, size.height * 0.82f),
                )
            }
            .clearAndSetSemantics {},
    ) {
        LeafIcon(
            color = palette.decoration.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = CalculinoSpacing.lg, top = CalculinoSpacing.xxl)
                .size(CalculinoSizes.decorationLarge)
                .rotate(-24f),
        )
        PawIcon(
            color = palette.decorationAccent.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = CalculinoSpacing.xl, end = CalculinoSpacing.xl)
                .size(CalculinoSizes.decorationMedium)
                .rotate(18f),
        )
        SparkleIcon(
            color = palette.decoration.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = CalculinoSpacing.xxl + CalculinoSpacing.xl, end = CalculinoSpacing.md)
                .size(CalculinoSizes.decorationSmall),
        )
        MushroomIcon(
            capColor = palette.decorationAccent.copy(alpha = 0.08f),
            stemColor = palette.decoration.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = CalculinoSpacing.sm, bottom = CalculinoSpacing.xxl)
                .size(CalculinoSizes.decorationLarge),
        )
        LeafIcon(
            color = palette.decoration.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = CalculinoSpacing.lg, bottom = CalculinoSpacing.xxl)
                .size(CalculinoSizes.decorationMedium)
                .rotate(140f),
        )
    }
}
