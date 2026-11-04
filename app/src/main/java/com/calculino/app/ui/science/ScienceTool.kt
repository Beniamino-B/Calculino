package com.calculino.app.ui.science

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.calculino.app.R

enum class ScienceCategory(
    @StringRes val labelRes: Int,
) {
    Chemistry(R.string.science_section_chemistry),
    Mathematics(R.string.science_section_mathematics),
    Physics(R.string.science_section_physics),
    Programming(R.string.science_section_programming),
    Engineering(R.string.science_section_engineering),
}

enum class ScienceTool(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val category: ScienceCategory,
) {
    PeriodicTable(
        route = "tools/periodic",
        titleRes = R.string.tool_periodic_title,
        descriptionRes = R.string.tool_periodic_description,
        icon = Icons.Rounded.TableChart,
        category = ScienceCategory.Chemistry,
    ),
    MolarMass(
        route = "tools/molar",
        titleRes = R.string.tool_molar_title,
        descriptionRes = R.string.tool_molar_description,
        icon = Icons.Rounded.Functions,
        category = ScienceCategory.Chemistry,
    ),
    Gibbs(
        route = "tools/gibbs",
        titleRes = R.string.tool_gibbs_title,
        descriptionRes = R.string.tool_gibbs_description,
        icon = Icons.Rounded.Thermostat,
        category = ScienceCategory.Chemistry,
    ),
    EmpiricalFormula(
        route = "tools/empirical",
        titleRes = R.string.tool_empirical_title,
        descriptionRes = R.string.tool_empirical_description,
        icon = Icons.Rounded.Science,
        category = ScienceCategory.Chemistry,
    ),
    Ph(
        route = "tools/ph",
        titleRes = R.string.tool_ph_title,
        descriptionRes = R.string.tool_ph_description,
        icon = Icons.Rounded.WaterDrop,
        category = ScienceCategory.Chemistry,
    ),
    Algebra(
        route = "tools/algebra",
        titleRes = R.string.tool_algebra_title,
        descriptionRes = R.string.tool_algebra_description,
        icon = Icons.Rounded.Functions,
        category = ScienceCategory.Mathematics,
    ),
    Matrix(
        route = "tools/matrix",
        titleRes = R.string.tool_matrix_title,
        descriptionRes = R.string.tool_matrix_description,
        icon = Icons.Rounded.GridOn,
        category = ScienceCategory.Mathematics,
    ),
    Statistics(
        route = "tools/statistics",
        titleRes = R.string.tool_statistics_title,
        descriptionRes = R.string.tool_statistics_description,
        icon = Icons.Rounded.QueryStats,
        category = ScienceCategory.Mathematics,
    ),
    Complex(
        route = "tools/complex",
        titleRes = R.string.tool_complex_title,
        descriptionRes = R.string.tool_complex_description,
        icon = Icons.Rounded.Calculate,
        category = ScienceCategory.Mathematics,
    ),
    Geometry(
        route = "tools/geometry",
        titleRes = R.string.tool_geometry_title,
        descriptionRes = R.string.tool_geometry_description,
        icon = Icons.Rounded.Category,
        category = ScienceCategory.Mathematics,
    ),
    Physics(
        route = "tools/physics",
        titleRes = R.string.tool_physics_title,
        descriptionRes = R.string.tool_physics_description,
        icon = Icons.Rounded.Bolt,
        category = ScienceCategory.Physics,
    ),
    BaseConverter(
        route = "tools/base",
        titleRes = R.string.tool_base_title,
        descriptionRes = R.string.tool_base_description,
        icon = Icons.Rounded.SwapHoriz,
        category = ScienceCategory.Programming,
    ),
    Subnet(
        route = "tools/subnet",
        titleRes = R.string.tool_subnet_title,
        descriptionRes = R.string.tool_subnet_description,
        icon = Icons.Rounded.Router,
        category = ScienceCategory.Programming,
    ),
    JsonXml(
        route = "tools/json-xml",
        titleRes = R.string.tool_json_xml_title,
        descriptionRes = R.string.tool_json_xml_description,
        icon = Icons.Rounded.DataObject,
        category = ScienceCategory.Programming,
    ),
    Complexity(
        route = "tools/complexity",
        titleRes = R.string.tool_complexity_title,
        descriptionRes = R.string.tool_complexity_description,
        icon = Icons.Rounded.Speed,
        category = ScienceCategory.Programming,
    ),
    Currency(
        route = "tools/currency",
        titleRes = R.string.tool_currency_title,
        descriptionRes = R.string.tool_currency_description,
        icon = Icons.Rounded.CurrencyExchange,
        category = ScienceCategory.Engineering,
    ),
}
