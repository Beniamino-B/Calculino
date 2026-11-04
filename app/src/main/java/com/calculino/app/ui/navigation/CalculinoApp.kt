package com.calculino.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.calculino.app.AppContainer
import com.calculino.app.R
import com.calculino.app.domain.ai.VerifiedCalculation
import com.calculino.app.domain.math.AngleMode
import com.calculino.app.ui.about.AboutRoute
import com.calculino.app.ui.ai.AiRoute
import com.calculino.app.ui.algebra.AlgebraRoute
import com.calculino.app.ui.calculator.CalculatorRoute
import com.calculino.app.ui.chemistry.EmpiricalFormulaRoute
import com.calculino.app.ui.chemistry.GibbsRoute
import com.calculino.app.ui.chemistry.MolarMassRoute
import com.calculino.app.ui.chemistry.PhRoute
import com.calculino.app.ui.engineering.BaseConverterRoute
import com.calculino.app.ui.engineering.ComplexityRoute
import com.calculino.app.ui.engineering.CurrencyRoute
import com.calculino.app.ui.engineering.JsonXmlRoute
import com.calculino.app.ui.engineering.MatrixRoute
import com.calculino.app.ui.engineering.SubnetRoute
import com.calculino.app.ui.geometry.GeometryRoute
import com.calculino.app.ui.mathematics.ComplexRoute
import com.calculino.app.ui.mathematics.StatisticsRoute
import com.calculino.app.ui.physics.PhysicsRoute
import com.calculino.app.ui.practice.PracticeRoute
import com.calculino.app.ui.science.PeriodicTableRoute
import com.calculino.app.ui.science.ScienceScreen
import com.calculino.app.ui.components.rememberHaptics
import com.calculino.app.ui.science.ScienceTool
import com.calculino.app.ui.scanner.CameraOcrRoute
import com.calculino.app.ui.settings.SettingsRoute
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import com.calculino.app.ui.theme.PillShape


@Composable
fun CalculinoApp(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val palette = LocalKawaiiPalette.current
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = null,
    )
    val destinations = TopLevelDestination.entries.filter { destination ->
        destination != TopLevelDestination.Assistant || settings?.localAiEnabled == true
    }
    val showBottomBar = currentRoute == null || destinations.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier,
        containerColor = palette.background,
        bottomBar = {
            if (showBottomBar) {
                CalculinoBottomBar(
                    currentRoute = currentRoute,
                    destinations = destinations,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Calculator.route,
            modifier = Modifier
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(palette.background, palette.backgroundEnd),
                    ),
                ),
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 180))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 120))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 180))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 120))
            },
        ) {
            composable(TopLevelDestination.Calculator.route) { entry ->
                val scannedExpression by entry.savedStateHandle
                    .getStateFlow(OCR_RESULT_KEY, "")
                    .collectAsStateWithLifecycle()
                CalculatorRoute(
                    historyRepository = container.historyRepository,
                    currencyRepository = container.currencyRepository,
                    scannedExpression = scannedExpression,
                    cameraOcrEnabled = settings?.cameraOcrEnabled == true,
                    onOpenScanner = {
                        navController.navigate(SCANNER_ROUTE) { launchSingleTop = true }
                    },
                    onScannedExpressionConsumed = {
                        entry.savedStateHandle[OCR_RESULT_KEY] = ""
                    },
                    onExplain = { calculation ->
                        navController.navigate(aiRoute(calculation)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(SCANNER_ROUTE) {
                CameraOcrRoute(
                    ocrEngine = container.ocrEngine,
                    onRecognized = { formula ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(OCR_RESULT_KEY, formula)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(TopLevelDestination.Assistant.route) {
                AiRoute(
                    container = container,
                    request = null,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(TopLevelDestination.Tools.route) {
                ScienceScreen(
                    onOpenTool = { tool ->
                        navController.navigate(tool.route) { launchSingleTop = true }
                    },
                )
            }
            composable(ScienceTool.PeriodicTable.route) {
                PeriodicTableRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.MolarMass.route) {
                MolarMassRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Physics.route) {
                PhysicsRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Geometry.route) {
                GeometryRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Gibbs.route) {
                GibbsRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.EmpiricalFormula.route) {
                EmpiricalFormulaRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Ph.route) {
                PhRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Statistics.route) {
                StatisticsRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Complex.route) {
                ComplexRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.JsonXml.route) {
                JsonXmlRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Complexity.route) {
                ComplexityRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Algebra.route) {
                AlgebraRoute(
                    mathJsRepository = container.mathJsRepository,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ScienceTool.BaseConverter.route) {
                BaseConverterRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Subnet.route) {
                SubnetRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Matrix.route) {
                MatrixRoute(onBack = { navController.popBackStack() })
            }
            composable(ScienceTool.Currency.route) {
                CurrencyRoute(
                    repository = container.currencyRepository,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(TopLevelDestination.Practice.route) {
                PracticeRoute()
            }
            composable(TopLevelDestination.Settings.route) {
                SettingsRoute(
                    settingsRepository = container.settingsRepository,
                    historyRepository = container.historyRepository,
                    onOpenAi = {
                        navController.navigate(TopLevelDestination.Assistant.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenAbout = {
                        navController.navigate(ABOUT_ROUTE) { launchSingleTop = true }
                    },
                )
            }
            composable(ABOUT_ROUTE) {
                AboutRoute(onBack = { navController.popBackStack() })
            }
            composable(
                route = AI_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument("expression") { defaultValue = "" },
                    navArgument("result") { defaultValue = "" },
                    navArgument("exact") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("approximation") { defaultValue = "" },
                    navArgument("angle") { defaultValue = AngleMode.Radians.name },
                ),
            ) { entry ->
                val expression = entry.arguments?.getString("expression").orEmpty()
                val result = entry.arguments?.getString("result").orEmpty()
                val approximation = entry.arguments?.getString("approximation")
                    ?.takeIf { it.isNotBlank() }
                val angleMode = entry.arguments?.getString("angle")
                    ?.let { stored -> AngleMode.entries.firstOrNull { it.name == stored } }
                    ?: AngleMode.Radians
                val request = if (expression.isNotBlank() && result.isNotBlank()) {
                    VerifiedCalculation(
                        expression = expression,
                        result = result,
                        approximation = approximation,
                        isExact = entry.arguments?.getBoolean("exact") ?: false,
                        angleMode = angleMode,
                    )
                } else {
                    null
                }
                AiRoute(
                    container = container,
                    request = request,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun CalculinoBottomBar(
    currentRoute: String?,
    destinations: List<TopLevelDestination>,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    val haptic = rememberHaptics()
    val selectedContentColor = MaterialTheme.colorScheme.onSurface
    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedIndex = destinations.indexOfFirst { destination ->
        destination.route == currentRoute
    }.coerceAtLeast(0)
    Surface(
        color = palette.surfaceMuted,
        shape = RoundedCornerShape(
            topStart = CalculinoSizes.navTopRadius,
            topEnd = CalculinoSizes.navTopRadius,
        ),
        tonalElevation = CalculinoSizes.noElevation,
        shadowElevation = CalculinoSizes.noElevation,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = CalculinoSpacing.sm,
                    vertical = CalculinoSpacing.sm,
                ),
        ) {
            val itemWidth = maxWidth / destinations.size.toFloat()
            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex.toFloat() +
                    (itemWidth - CalculinoSizes.navIndicatorWidth) / 2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "navPillOffset",
            )
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = indicatorOffset.roundToPx(),
                            y = CalculinoSpacing.xs.roundToPx(),
                        )
                    }
                    .width(CalculinoSizes.navIndicatorWidth)
                    .height(CalculinoSizes.navIndicatorHeight)
                    .clip(PillShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                destinations.forEach { destination ->
                    val selected = destination.route == currentRoute
                    val label = stringResource(destination.labelRes)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(CalculinoSizes.navItemHeight)
                            .selectable(
                                selected = selected,
                                role = Role.Tab,
                                onClick = {
                                    haptic(HapticFeedbackType.TextHandleMove)
                                    onNavigate(destination)
                                },
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            tint = if (selected) selectedContentColor else unselectedContentColor,
                            modifier = Modifier.size(CalculinoSizes.iconMedium),
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) selectedContentColor else unselectedContentColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private const val ABOUT_ROUTE = "about"
private const val SCANNER_ROUTE = "scanner"
private const val OCR_RESULT_KEY = "ocr_result"
private const val AI_ROUTE_BASE = "ai"
private const val AI_ROUTE_PATTERN =
    "ai?expression={expression}&result={result}&exact={exact}&approximation={approximation}&angle={angle}"

private fun aiRoute(calculation: VerifiedCalculation): String = buildString {
    append(AI_ROUTE_BASE)
    append("?expression=")
    append(Uri.encode(calculation.expression))
    append("&result=")
    append(Uri.encode(calculation.result))
    append("&exact=")
    append(calculation.isExact)
    append("&approximation=")
    append(Uri.encode(calculation.approximation.orEmpty()))
    append("&angle=")
    append(Uri.encode(calculation.angleMode.name))
}
