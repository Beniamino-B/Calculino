package com.calculino.app.ui.science

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.calculino.app.R
import com.calculino.app.domain.periodic.ElementCategory
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun elementCategoryContainer(category: ElementCategory): Color {
    val palette = LocalKawaiiPalette.current
    return when (category) {
        ElementCategory.AlkaliMetal -> palette.keyDivide
        ElementCategory.AlkalineEarth -> palette.keyCurrency
        ElementCategory.TransitionMetal -> palette.keyNumber
        ElementCategory.PostTransitionMetal -> palette.keyFunction
        ElementCategory.Metalloid -> palette.keyMultiply
        ElementCategory.Nonmetal -> palette.keyPlus
        ElementCategory.NobleGas -> palette.keyPercent
        ElementCategory.Lanthanide -> palette.keyDelete
        ElementCategory.Actinide -> palette.keyDecimal
        ElementCategory.Unknown -> palette.surfaceStrong
    }
}

@Composable
fun elementCategoryContent(category: ElementCategory): Color {
    val palette = LocalKawaiiPalette.current
    return when (category) {
        ElementCategory.AlkaliMetal -> palette.onKeyDivide
        ElementCategory.AlkalineEarth -> palette.onKeyCurrency
        ElementCategory.TransitionMetal -> palette.onKeyNumber
        ElementCategory.PostTransitionMetal -> palette.onKeyFunction
        ElementCategory.Metalloid -> palette.onKeyMultiply
        ElementCategory.Nonmetal -> palette.onKeyPlus
        ElementCategory.NobleGas -> palette.onKeyPercent
        ElementCategory.Lanthanide -> palette.onKeyDelete
        ElementCategory.Actinide -> palette.onKeyDecimal
        ElementCategory.Unknown -> palette.textSecondary
    }
}

@Composable
fun elementCategoryLabel(category: ElementCategory): String = stringResource(
    when (category) {
        ElementCategory.AlkaliMetal -> R.string.category_alkali_metal
        ElementCategory.AlkalineEarth -> R.string.category_alkaline_earth
        ElementCategory.TransitionMetal -> R.string.category_transition_metal
        ElementCategory.PostTransitionMetal -> R.string.category_post_transition
        ElementCategory.Metalloid -> R.string.category_metalloid
        ElementCategory.Nonmetal -> R.string.category_nonmetal
        ElementCategory.NobleGas -> R.string.category_noble_gas
        ElementCategory.Lanthanide -> R.string.category_lanthanide
        ElementCategory.Actinide -> R.string.category_actinide
        ElementCategory.Unknown -> R.string.category_unknown
    },
)
