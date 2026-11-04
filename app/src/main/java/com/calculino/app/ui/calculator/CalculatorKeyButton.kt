package com.calculino.app.ui.calculator

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.calculino.app.ui.components.LeafIcon
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.PawIcon
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.KawaiiPalette
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape

enum class CalcKeyKind {
    Digit,
    Plus,
    Minus,
    Multiply,
    Divide,
    Percent,
    Equals,
    Clear,
    Delete,
    Decimal,
    Currency,
    Function,
}

data class CalcKeySpec(
    val label: String,
    val description: String,
    val kind: CalcKeyKind,
    val token: String? = label,
    val span: Int = 1,
)

fun KawaiiPalette.containerFor(kind: CalcKeyKind): Color = when {
    kind == CalcKeyKind.Digit || kind == CalcKeyKind.Decimal -> keyNumber
    kind == CalcKeyKind.Plus -> keyPlus
    kind == CalcKeyKind.Minus -> keyMinus
    kind == CalcKeyKind.Multiply -> keyMultiply
    kind == CalcKeyKind.Divide -> keyDivide
    kind == CalcKeyKind.Percent -> keyPercent
    kind == CalcKeyKind.Equals -> keyEquals
    kind == CalcKeyKind.Clear -> keyClear
    kind == CalcKeyKind.Delete -> keyDelete
    kind == CalcKeyKind.Currency -> keyCurrency
    else -> keyFunction
}

fun KawaiiPalette.contentFor(kind: CalcKeyKind): Color = when {
    kind == CalcKeyKind.Digit || kind == CalcKeyKind.Decimal -> onKeyNumber
    kind == CalcKeyKind.Plus -> onKeyPlus
    kind == CalcKeyKind.Minus -> onKeyMinus
    kind == CalcKeyKind.Multiply -> onKeyMultiply
    kind == CalcKeyKind.Divide -> onKeyDivide
    kind == CalcKeyKind.Percent -> onKeyPercent
    kind == CalcKeyKind.Equals -> onKeyEquals
    kind == CalcKeyKind.Clear -> onKeyClear
    kind == CalcKeyKind.Delete -> onKeyDelete
    kind == CalcKeyKind.Currency -> onKeyCurrency
    else -> onKeyFunction
}

fun KawaiiPalette.textStyleFor(kind: CalcKeyKind, base: TextStyle): TextStyle = when (kind) {
    CalcKeyKind.Function, CalcKeyKind.Currency -> base.copy(fontSize = base.fontSize * 0.72f)
    else -> base
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorKeyButton(
    spec: CalcKeySpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = LocalKawaiiPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "keyScale",
    )
    val shape = PillShape
    val containerColor = palette.containerFor(spec.kind)
    val contentColor = palette.contentFor(spec.kind)
    val rippleIndication = ripple(color = contentColor)
    val borderModifier = if (focused) {
        Modifier.border(CalculinoSizes.focusBorder, palette.focusRing, shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .shadow(
                elevation = if (pressed) CalculinoSizes.noElevation else CalculinoSizes.keyElevation,
                shape = shape,
                clip = false,
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        lerp(containerColor, Color.White, 0.34f),
                        containerColor,
                        lerp(containerColor, Color.Black, 0.08f),
                    ),
                ),
            )
            .border(CalculinoSizes.hairline, Color.White.copy(alpha = 0.28f), shape)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = rippleIndication,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = spec.description },
        contentAlignment = Alignment.Center,
    ) {
        when (spec.kind) {
            CalcKeyKind.Clear -> LeafIcon(
                color = contentColor.copy(alpha = 0.35f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = CalculinoSpacing.xs, bottom = CalculinoSpacing.xs)
                    .size(CalculinoSizes.iconSmall),
            )
            CalcKeyKind.Equals -> MushroomIcon(
                capColor = Color.White.copy(alpha = 0.24f),
                stemColor = Color.White.copy(alpha = 0.18f),
                dotColor = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = CalculinoSpacing.xs, bottom = CalculinoSpacing.xs)
                    .size(CalculinoSizes.mushroomBackdrop),
            )
            CalcKeyKind.Delete -> PawIcon(
                color = contentColor,
                modifier = Modifier.size(CalculinoSizes.pawIcon),
            )
            else -> Unit
        }
        if (spec.label.isNotEmpty()) {
            Text(
                text = spec.label,
                color = contentColor,
                style = palette.textStyleFor(spec.kind, MaterialTheme.typography.displaySmall),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
