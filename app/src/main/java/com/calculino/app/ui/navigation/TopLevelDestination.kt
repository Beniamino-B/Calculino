package com.calculino.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector
import com.calculino.app.R

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Calculator(
        route = "calculator",
        labelRes = R.string.nav_calculator,
        icon = Icons.Rounded.Calculate,
    ),
    Assistant(
        route = "assistant",
        labelRes = R.string.nav_assistant,
        icon = Icons.Rounded.SmartToy,
    ),
    Tools(
        route = "tools",
        labelRes = R.string.nav_tools,
        icon = Icons.Rounded.Science,
    ),
    Practice(
        route = "practice",
        labelRes = R.string.nav_practice,
        icon = Icons.Rounded.School,
    ),
    Settings(
        route = "settings",
        labelRes = R.string.nav_settings,
        icon = Icons.Rounded.Settings,
    ),
}
