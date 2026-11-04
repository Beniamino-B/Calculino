package com.calculino.app.ui.science

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.calculino.app.R
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.PawIcon
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun ScienceScreen(
    onOpenTool: (ScienceTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        KawaiiBackdrop()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = CalculinoSizes.screenMaxWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = CalculinoSpacing.lg,
                    vertical = CalculinoSpacing.sm,
                ),
        ) {
            ScienceHeader()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = CalculinoSpacing.md,
                    bottom = CalculinoSpacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                ScienceCategory.entries.forEach { category ->
                    val tools = ScienceTool.entries.filter { tool -> tool.category == category }
                    if (tools.isEmpty()) return@forEach
                    item(
                        key = "category_${category.name}",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        ScienceSectionTitle(text = stringResource(category.labelRes))
                    }
                    items(
                        items = tools,
                        key = { tool -> tool.route },
                        contentType = { "science_tool" },
                    ) { tool ->
                        ScienceToolCard(
                            tool = tool,
                            onClick = {
                                haptic(HapticFeedbackType.TextHandleMove)
                                onOpenTool(tool)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScienceHeader() {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
    ) {
        PawIcon(
            color = palette.decorationAccent,
            modifier = Modifier.size(CalculinoSizes.decorationMedium),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.science_title),
                style = MaterialTheme.typography.headlineLarge,
                color = palette.title,
            )
            Text(
                text = stringResource(R.string.science_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
        Surface(
            shape = CircleShape,
            color = palette.surfaceMuted,
            border = BorderStroke(CalculinoSizes.hairline, palette.border),
            modifier = Modifier.size(CalculinoSizes.minTouchTarget),
        ) {
            Box(contentAlignment = Alignment.Center) {
                MushroomIcon(
                    capColor = palette.title,
                    stemColor = palette.title.copy(alpha = 0.75f),
                    dotColor = palette.surface,
                    modifier = Modifier.size(CalculinoSizes.iconLarge),
                )
            }
        }
    }
}

@Composable
private fun ScienceToolCard(
    tool: ScienceTool,
    onClick: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CalculinoSizes.scienceCardMinHeight)
            .kawaiiCard()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(CalculinoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = palette.surfaceMuted,
                modifier = Modifier.size(CalculinoSizes.minTouchTarget),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = palette.title,
                        modifier = Modifier.size(CalculinoSizes.iconMedium),
                    )
                }
            }
            Text(
                text = stringResource(tool.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(tool.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ScienceSectionTitle(text: String) {
    val palette = LocalKawaiiPalette.current
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = palette.textSecondary,
        modifier = Modifier.padding(top = CalculinoSpacing.sm),
    )
}
