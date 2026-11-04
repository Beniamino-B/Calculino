package com.calculino.app.ui.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.calculino.app.BuildConfig
import com.calculino.app.R
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.components.MushroomIcon
import com.calculino.app.ui.components.PawIcon
import com.calculino.app.ui.components.kawaiiCard
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette

@Composable
fun AboutRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AboutScreen(onBack = onBack, modifier = modifier)
}

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current

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
                .padding(horizontal = CalculinoSpacing.lg),
        ) {
            ScienceToolHeader(
                title = stringResource(R.string.about_title),
                subtitle = stringResource(R.string.about_subtitle),
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = CalculinoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            ) {
                HeroCard()
                InfoCard(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.about_privacy_title),
                    body = stringResource(R.string.about_privacy_body),
                )
                InfoCard(
                    icon = Icons.Rounded.SmartToy,
                    title = stringResource(R.string.about_ai_title),
                    body = stringResource(R.string.about_ai_body),
                )
                InfoCard(
                    icon = Icons.Rounded.Code,
                    title = stringResource(R.string.about_licenses_title),
                    body = stringResource(R.string.about_licenses_body),
                )
                InfoCard(
                    icon = Icons.Rounded.Public,
                    title = stringResource(R.string.about_sources_title),
                    body = stringResource(R.string.about_sources_body),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        CalculinoSpacing.xs,
                        Alignment.CenterHorizontally,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PawIcon(
                        color = palette.decorationAccent,
                        modifier = Modifier.size(CalculinoSizes.iconSmall),
                    )
                    Text(
                        text = stringResource(R.string.about_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.height(CalculinoSpacing.xxl))
            }
        }
    }
}

@Composable
private fun HeroCard() {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CalculinoSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
        ) {
            Surface(
                shape = CircleShape,
                color = palette.surfaceMuted,
                modifier = Modifier.size(CalculinoSizes.heroIconContainer),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    MushroomIcon(
                        capColor = palette.title,
                        stemColor = palette.title.copy(alpha = 0.75f),
                        dotColor = palette.surface,
                        modifier = Modifier.size(CalculinoSizes.heroIcon),
                    )
                }
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = palette.title,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = palette.surfaceMuted,
            ) {
                Text(
                    text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(
                        horizontal = CalculinoSpacing.md,
                        vertical = CalculinoSpacing.xxs,
                    ),
                )
            }
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    body: String,
) {
    val palette = LocalKawaiiPalette.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = palette.surface,
        border = BorderStroke(CalculinoSizes.hairline, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .kawaiiCard(),
    ) {
        Row(
            modifier = Modifier.padding(CalculinoSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = CircleShape,
                color = palette.surfaceMuted,
                modifier = Modifier.size(CalculinoSizes.minTouchTarget),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.title,
                        modifier = Modifier.size(CalculinoSizes.iconMedium),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
        }
    }
}
