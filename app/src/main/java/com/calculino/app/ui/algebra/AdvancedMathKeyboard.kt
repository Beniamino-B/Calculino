package com.calculino.app.ui.algebra

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.calculino.app.R
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

private enum class MathKeyAction {
    Insert,
    Backspace,
    Clear,
}

private data class MathKey(
    val label: String,
    val insert: String = label,
    val action: MathKeyAction = MathKeyAction.Insert,
)

private data class MathTab(
    val labelRes: Int,
    val rows: List<List<MathKey>>,
)

private val mathTabs = listOf(
    MathTab(
        labelRes = R.string.algebra_keyboard_tab_basic,
        rows = listOf(
            listOf(
                MathKey("7"),
                MathKey("8"),
                MathKey("9"),
                MathKey("÷", "/"),
            ),
            listOf(
                MathKey("4"),
                MathKey("5"),
                MathKey("6"),
                MathKey("×", "*"),
            ),
            listOf(
                MathKey("1"),
                MathKey("2"),
                MathKey("3"),
                MathKey("−", "-"),
            ),
            listOf(
                MathKey("0"),
                MathKey("."),
                MathKey("="),
                MathKey("+"),
            ),
            listOf(
                MathKey("("),
                MathKey(")"),
                MathKey("√", "sqrt("),
                MathKey("^"),
            ),
            listOf(
                MathKey("π", "pi"),
                MathKey("x"),
                MathKey("⌫", action = MathKeyAction.Backspace),
                MathKey("C", action = MathKeyAction.Clear),
            ),
        ),
    ),
    MathTab(
        labelRes = R.string.algebra_keyboard_tab_functions,
        rows = listOf(
            listOf(
                MathKey("f(x)", "f("),
                MathKey("log(", "log("),
                MathKey("ln(", "ln("),
                MathKey("√(", "sqrt("),
            ),
            listOf(
                MathKey("abs(", "abs("),
                MathKey("exp(", "exp("),
                MathKey("^2", "^2"),
                MathKey("^3", "^3"),
            ),
            listOf(
                MathKey("π", "pi"),
                MathKey("e", "e"),
                MathKey("!"),
                MathKey("%"),
            ),
            listOf(
                MathKey("x"),
                MathKey("y"),
                MathKey("("),
                MathKey(")"),
            ),
            listOf(
                MathKey(","),
                MathKey("⌫", action = MathKeyAction.Backspace),
                MathKey("C", action = MathKeyAction.Clear),
                MathKey("="),
            ),
        ),
    ),
    MathTab(
        labelRes = R.string.algebra_keyboard_tab_trigonometry,
        rows = listOf(
            listOf(
                MathKey("sin(", "sin("),
                MathKey("cos(", "cos("),
                MathKey("tan(", "tan("),
                MathKey("π", "pi"),
            ),
            listOf(
                MathKey("asin(", "asin("),
                MathKey("acos(", "acos("),
                MathKey("atan(", "atan("),
                MathKey("°"),
            ),
            listOf(
                MathKey("sinh(", "sinh("),
                MathKey("cosh(", "cosh("),
                MathKey("tanh(", "tanh("),
                MathKey("e", "e"),
            ),
            listOf(
                MathKey("("),
                MathKey(")"),
                MathKey("x"),
                MathKey("y"),
            ),
            listOf(
                MathKey("^"),
                MathKey("⌫", action = MathKeyAction.Backspace),
                MathKey("C", action = MathKeyAction.Clear),
                MathKey("="),
            ),
        ),
    ),
    MathTab(
        labelRes = R.string.algebra_keyboard_tab_calculus,
        rows = listOf(
            listOf(
                MathKey("lim(", "lim("),
                MathKey("dx", "dx"),
                MathKey("∫(", "∫("),
                MathKey("∞", "∞"),
            ),
            listOf(
                MathKey("d/dx", "d/dx"),
                MathKey("∑(", "∑("),
                MathKey("Π(", "Π("),
                MathKey("→", "→"),
            ),
            listOf(
                MathKey("("),
                MathKey(")"),
                MathKey("x"),
                MathKey("y"),
            ),
            listOf(
                MathKey("^"),
                MathKey("√(", "sqrt("),
                MathKey(","),
                MathKey("="),
            ),
            listOf(
                MathKey("+"),
                MathKey("−", "-"),
                MathKey("×", "*"),
                MathKey("÷", "/"),
            ),
            listOf(
                MathKey("⌫", action = MathKeyAction.Backspace),
                MathKey("C", action = MathKeyAction.Clear),
            ),
        ),
    ),
)

@Composable
fun AdvancedMathKeyboard(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tab = mathTabs[selectedTab]

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = palette.title,
                divider = {},
            ) {
                mathTabs.forEachIndexed { index, mathTab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = stringResource(mathTab.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
                tab.rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
                    ) {
                        row.forEach { key ->
                            MathKeyButton(
                                key = key,
                                onInsert = onInsert,
                                onBackspace = onBackspace,
                                onClear = onClear,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(4 - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MathKeyButton(
    key: MathKey,
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val background = when (key.action) {
        MathKeyAction.Insert -> palette.surfaceMuted
        MathKeyAction.Backspace -> palette.keyDelete
        MathKeyAction.Clear -> palette.keyClear
    }
    val content = when (key.action) {
        MathKeyAction.Insert -> palette.textPrimary
        MathKeyAction.Backspace -> palette.onKeyDelete
        MathKeyAction.Clear -> palette.onKeyClear
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = background,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = modifier
            .height(CalculinoSizes.minTouchTarget)
            .clickable(
                role = Role.Button,
                onClick = {
                    when (key.action) {
                        MathKeyAction.Insert -> onInsert(key.insert)
                        MathKeyAction.Backspace -> onBackspace()
                        MathKeyAction.Clear -> onClear()
                    }
                },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key.label,
                style = MaterialTheme.typography.titleSmall,
                color = content,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = CalculinoSpacing.xxs),
            )
        }
    }
}
