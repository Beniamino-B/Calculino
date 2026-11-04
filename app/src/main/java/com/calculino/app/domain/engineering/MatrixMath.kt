package com.calculino.app.domain.engineering

data class NumericMatrix(
    val rows: Int,
    val cols: Int,
    val values: List<Double>,
) {
    init {
        require(rows > 0 && cols > 0) { "matrix dimensions must be positive" }
        require(values.size == rows * cols) { "matrix values do not match dimensions" }
    }

    operator fun get(row: Int, col: Int): Double = values[row * cols + col]
}

fun transposeMatrix(matrix: NumericMatrix): NumericMatrix {
    val result = MutableList(matrix.rows * matrix.cols) { 0.0 }
    for (row in 0 until matrix.rows) {
        for (col in 0 until matrix.cols) {
            result[col * matrix.rows + row] = matrix.values[row * matrix.cols + col]
        }
    }
    return NumericMatrix(rows = matrix.cols, cols = matrix.rows, values = result)
}

fun determinantMatrix(matrix: NumericMatrix): Double? {
    if (matrix.rows != matrix.cols) return null
    val values = matrix.values
    return when (matrix.rows) {
        1 -> values[0]
        2 -> values[0] * values[3] - values[1] * values[2]
        3 -> {
            val a = values[0]
            val b = values[1]
            val c = values[2]
            val d = values[3]
            val e = values[4]
            val f = values[5]
            val g = values[6]
            val h = values[7]
            val i = values[8]
            a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g)
        }
        else -> null
    }
}

fun inverseMatrix(matrix: NumericMatrix): NumericMatrix? {
    if (matrix.rows != matrix.cols) return null
    if (matrix.rows !in 1..3) return null
    val size = matrix.rows
    val augmented = Array(size) { row ->
        DoubleArray(size * 2) { column ->
            when {
                column < size -> matrix[row, column]
                column - size == row -> 1.0
                else -> 0.0
            }
        }
    }
    for (pivotColumn in 0 until size) {
        var pivotRow = pivotColumn
        while (pivotRow < size && augmented[pivotRow][pivotColumn].isNearlyZero()) pivotRow++
        if (pivotRow == size) return null
        if (pivotRow != pivotColumn) {
            val temporary = augmented[pivotRow]
            augmented[pivotRow] = augmented[pivotColumn]
            augmented[pivotColumn] = temporary
        }
        val pivot = augmented[pivotColumn][pivotColumn]
        for (column in 0 until size * 2) augmented[pivotColumn][column] /= pivot
        for (row in 0 until size) {
            if (row == pivotColumn) continue
            val factor = augmented[row][pivotColumn]
            if (factor.isNearlyZero()) continue
            for (column in 0 until size * 2) {
                augmented[row][column] -= factor * augmented[pivotColumn][column]
            }
        }
    }
    val inverse = DoubleArray(size * size)
    for (row in 0 until size) {
        for (column in 0 until size) {
            inverse[row * size + column] = augmented[row][size + column]
        }
    }
    return NumericMatrix(rows = size, cols = size, values = inverse.toList())
}

fun multiplyMatrices(first: NumericMatrix, second: NumericMatrix): NumericMatrix? {
    if (first.cols != second.rows) return null
    val values = DoubleArray(first.rows * second.cols)
    for (row in 0 until first.rows) {
        for (column in 0 until second.cols) {
            var total = 0.0
            for (index in 0 until first.cols) {
                total += first[row, index] * second[index, column]
            }
            values[row * second.cols + column] = total
        }
    }
    return NumericMatrix(
        rows = first.rows,
        cols = second.cols,
        values = values.toList(),
    )
}

fun Double.isNearlyZero(): Boolean = kotlin.math.abs(this) < 1e-12
