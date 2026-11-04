package com.calculino.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun PawIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val toeRadius = width * 0.115f
        val toes = listOf(
            Offset(width * 0.17f, height * 0.40f),
            Offset(width * 0.37f, height * 0.18f),
            Offset(width * 0.63f, height * 0.18f),
            Offset(width * 0.83f, height * 0.40f),
        )
        toes.forEach { center ->
            drawCircle(color = color, radius = toeRadius, center = center)
        }
        drawOval(
            color = color,
            topLeft = Offset(width * 0.22f, height * 0.46f),
            size = Size(width * 0.56f, height * 0.46f),
        )
    }
}

@Composable
fun LeafIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val leaf = Path().apply {
            moveTo(width * 0.12f, height * 0.88f)
            cubicTo(
                width * 0.18f, height * 0.30f,
                width * 0.58f, height * 0.06f,
                width * 0.92f, height * 0.14f,
            )
            cubicTo(
                width * 0.86f, height * 0.60f,
                width * 0.48f, height * 0.92f,
                width * 0.12f, height * 0.88f,
            )
            close()
        }
        drawPath(
            path = leaf,
            color = color,
            style = Stroke(
                width = width * 0.085f,
                cap = StrokeCap.Round,
            ),
        )
        drawLine(
            color = color,
            start = Offset(width * 0.20f, height * 0.82f),
            end = Offset(width * 0.80f, height * 0.22f),
            strokeWidth = width * 0.06f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(width * 0.12f, height * 0.88f),
            end = Offset(width * 0.04f, height * 0.98f),
            strokeWidth = width * 0.075f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun MushroomIcon(
    capColor: Color,
    modifier: Modifier = Modifier,
    stemColor: Color = capColor,
    dotColor: Color? = null,
) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val cap = Path().apply {
            moveTo(width * 0.06f, height * 0.54f)
            cubicTo(
                width * 0.14f, height * 0.16f,
                width * 0.86f, height * 0.16f,
                width * 0.94f, height * 0.54f,
            )
            close()
        }
        drawPath(path = cap, color = capColor)
        drawRoundRect(
            color = stemColor,
            topLeft = Offset(width * 0.38f, height * 0.48f),
            size = Size(width * 0.24f, height * 0.42f),
            cornerRadius = CornerRadius(width * 0.09f, width * 0.09f),
        )
        if (dotColor != null) {
            drawCircle(
                color = dotColor,
                radius = width * 0.06f,
                center = Offset(width * 0.32f, height * 0.38f),
            )
            drawCircle(
                color = dotColor,
                radius = width * 0.075f,
                center = Offset(width * 0.56f, height * 0.30f),
            )
            drawCircle(
                color = dotColor,
                radius = width * 0.05f,
                center = Offset(width * 0.73f, height * 0.42f),
            )
        }
    }
}

@Composable
fun SparkleIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val sparkle = Path().apply {
            moveTo(width * 0.5f, 0f)
            quadraticTo(width * 0.58f, height * 0.42f, width, height * 0.5f)
            quadraticTo(width * 0.58f, height * 0.58f, width * 0.5f, height)
            quadraticTo(width * 0.42f, height * 0.58f, 0f, height * 0.5f)
            quadraticTo(width * 0.42f, height * 0.42f, width * 0.5f, 0f)
            close()
        }
        drawPath(path = sparkle, color = color)
    }
}
