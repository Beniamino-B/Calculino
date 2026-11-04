package com.calculino.app.ui.science

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import com.calculino.app.R
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun ScienceToolHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        IconButton(
            onClick = {
                haptic(HapticFeedbackType.TextHandleMove)
                onBack()
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = palette.textPrimary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.title,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                maxLines = 2,
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
