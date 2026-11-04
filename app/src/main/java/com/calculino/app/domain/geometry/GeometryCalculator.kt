package com.calculino.app.domain.geometry

import kotlin.math.hypot

enum class GeometryShape {
    Circle,
    Rectangle,
    RightTriangle,
}

data class GeometryMeasurements(
    val area: Double,
    val perimeter: Double,
)

object GeometryCalculator {
    fun circle(radius: Double): GeometryMeasurements = GeometryMeasurements(
        area = Math.PI * radius * radius,
        perimeter = 2.0 * Math.PI * radius,
    )

    fun rectangle(width: Double, height: Double): GeometryMeasurements = GeometryMeasurements(
        area = width * height,
        perimeter = 2.0 * (width + height),
    )

    fun rightTriangle(base: Double, height: Double): GeometryMeasurements = GeometryMeasurements(
        area = base * height / 2.0,
        perimeter = base + height + hypot(base, height),
    )
}
