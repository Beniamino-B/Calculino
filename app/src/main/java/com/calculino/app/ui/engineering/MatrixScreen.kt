package com.calculino.app.ui.engineering

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.domain.engineering.NumericMatrix
import com.calculino.app.domain.engineering.determinantMatrix
import com.calculino.app.domain.engineering.inverseMatrix
import com.calculino.app.domain.engineering.isNearlyZero
import com.calculino.app.domain.engineering.multiplyMatrices
import com.calculino.app.domain.engineering.transposeMatrix
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.round

enum class MatrixOperation {
    Determinant,
    Transpose,
    Inverse,
    Multiply,
}

data class MatrixUiState(
    val rows: Int = 2,
    val cols: Int = 2,
    val cells: List<String> = identityCells(2),
    val secondRows: Int = 2,
    val secondCols: Int = 2,
    val secondCells: List<String> = identityCells(2),
    val operation: MatrixOperation = MatrixOperation.Determinant,
    val determinant: Double? = null,
    val transposed: NumericMatrix? = null,
    val inverse: NumericMatrix? = null,
    val product: NumericMatrix? = null,
    val resultComputed: Boolean = false,
    val invalidCells: Boolean = false,
)

class MatrixViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(MatrixUiState())
    val uiState: StateFlow<MatrixUiState> = mutableState.asStateFlow()

    fun onRowsChange(rows: Int) {
        mutableState.update { state ->
            state.copy(
                rows = rows,
                cells = resizeCells(state.cells, state.rows, state.cols, rows, state.cols),
            ).clearResults()
        }
    }

    fun onColsChange(cols: Int) {
        mutableState.update { state ->
            state.copy(
                cols = cols,
                cells = resizeCells(state.cells, state.rows, state.cols, state.rows, cols),
            ).clearResults()
        }
    }

    fun onCellChange(index: Int, value: String) {
        mutableState.update { state ->
            val cells = state.cells.toMutableList()
            if (index in cells.indices) cells[index] = value
            state.copy(cells = cells).clearResults()
        }
    }

    fun onSecondRowsChange(rows: Int) {
        mutableState.update { state ->
            state.copy(
                secondRows = rows,
                secondCells = resizeCells(
                    state.secondCells,
                    state.secondRows,
                    state.secondCols,
                    rows,
                    state.secondCols,
                ),
            ).clearResults()
        }
    }

    fun onSecondColsChange(cols: Int) {
        mutableState.update { state ->
            state.copy(
                secondCols = cols,
                secondCells = resizeCells(
                    state.secondCells,
                    state.secondRows,
                    state.secondCols,
                    state.secondRows,
                    cols,
                ),
            ).clearResults()
        }
    }

    fun onSecondCellChange(index: Int, value: String) {
        mutableState.update { state ->
            val cells = state.secondCells.toMutableList()
            if (index in cells.indices) cells[index] = value
            state.copy(secondCells = cells).clearResults()
        }
    }

    fun onOperationChange(operation: MatrixOperation) {
        mutableState.update { state -> state.copy(operation = operation).clearResults() }
    }

    fun calculate() {
        mutableState.update { state ->
            val firstValues = state.cells.map { cell -> cell.trim().replace(',', '.').toDoubleOrNull() }
            val secondValues = if (state.operation == MatrixOperation.Multiply) {
                state.secondCells.map { cell -> cell.trim().replace(',', '.').toDoubleOrNull() }
            } else {
                emptyList()
            }
            if (firstValues.any { value -> value == null } || secondValues.any { value -> value == null }) {
                return@update state.copy(
                    resultComputed = false,
                    invalidCells = true,
                )
            }
            val first = NumericMatrix(
                rows = state.rows,
                cols = state.cols,
                values = firstValues.map { value -> value ?: 0.0 },
            )
            val second = if (state.operation == MatrixOperation.Multiply) {
                NumericMatrix(
                    rows = state.secondRows,
                    cols = state.secondCols,
                    values = secondValues.map { value -> value ?: 0.0 },
                )
            } else {
                null
            }
            val determinant = if (state.operation == MatrixOperation.Determinant) {
                determinantMatrix(first)
            } else {
                null
            }
            val transposed = if (state.operation == MatrixOperation.Transpose) {
                transposeMatrix(first)
            } else {
                null
            }
            val inverse = if (state.operation == MatrixOperation.Inverse) {
                inverseMatrix(first)
            } else {
                null
            }
            val product = if (second != null) multiplyMatrices(first, second) else null
            state.copy(
                determinant = determinant,
                transposed = transposed,
                inverse = inverse,
                product = product,
                resultComputed = true,
                invalidCells = false,
            )
        }
    }

    private fun MatrixUiState.clearResults(): MatrixUiState = copy(
        determinant = null,
        transposed = null,
        inverse = null,
        product = null,
        resultComputed = false,
        invalidCells = false,
    )
}

private fun identityCells(size: Int): List<String> =
    List(size * size) { index -> if (index / size == index % size) "1" else "0" }

private fun resizeCells(
    cells: List<String>,
    oldRows: Int,
    oldCols: Int,
    newRows: Int,
    newCols: Int,
): List<String> {
    val resized = MutableList(newRows * newCols) { index ->
        val row = index / newCols
        val col = index % newCols
        if (row == col) "1" else "0"
    }
    for (row in 0 until minOf(oldRows, newRows)) {
        for (col in 0 until minOf(oldCols, newCols)) {
            resized[row * newCols + col] = cells[row * oldCols + col]
        }
    }
    return resized
}

@Composable
fun MatrixRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MatrixViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MatrixScreen(
        state = state,
        onRowsChange = viewModel::onRowsChange,
        onColsChange = viewModel::onColsChange,
        onCellChange = viewModel::onCellChange,
        onSecondRowsChange = viewModel::onSecondRowsChange,
        onSecondColsChange = viewModel::onSecondColsChange,
        onSecondCellChange = viewModel::onSecondCellChange,
        onOperationChange = viewModel::onOperationChange,
        onCalculate = viewModel::calculate,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun MatrixScreen(
    state: MatrixUiState,
    onRowsChange: (Int) -> Unit,
    onColsChange: (Int) -> Unit,
    onCellChange: (Int, String) -> Unit,
    onSecondRowsChange: (Int) -> Unit,
    onSecondColsChange: (Int) -> Unit,
    onSecondCellChange: (Int, String) -> Unit,
    onOperationChange: (MatrixOperation) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    EngineeringToolScaffold(
        title = stringResource(R.string.tool_matrix_title),
        subtitle = stringResource(R.string.tool_matrix_description),
        onBack = onBack,
        modifier = modifier,
    ) {
        EngineeringCard(title = stringResource(R.string.matrix_operation)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
            ) {
                MatrixOperation.entries.forEach { operation ->
                    FilterChip(
                        selected = state.operation == operation,
                        onClick = { onOperationChange(operation) },
                        label = { Text(matrixOperationLabel(operation), maxLines = 1) },
                    )
                }
            }
        }
        EngineeringCard(title = stringResource(R.string.matrix_first)) {
            MatrixDimensions(
                rows = state.rows,
                cols = state.cols,
                onRowsChange = onRowsChange,
                onColsChange = onColsChange,
            )
            MatrixInput(
                rows = state.rows,
                cols = state.cols,
                cells = state.cells,
                onCellChange = onCellChange,
                descriptionPrefix = stringResource(R.string.matrix_first),
            )
        }
        if (state.operation == MatrixOperation.Multiply) {
            EngineeringCard(title = stringResource(R.string.matrix_second)) {
                MatrixDimensions(
                    rows = state.secondRows,
                    cols = state.secondCols,
                    onRowsChange = onSecondRowsChange,
                    onColsChange = onSecondColsChange,
                )
                MatrixInput(
                    rows = state.secondRows,
                    cols = state.secondCols,
                    cells = state.secondCells,
                    onCellChange = onSecondCellChange,
                    descriptionPrefix = stringResource(R.string.matrix_second),
                )
            }
        }
        if (state.invalidCells) {
            EngineeringError(stringResource(R.string.matrix_invalid_cells))
        }
        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.matrix_calculate))
        }
        if (state.resultComputed) {
            when (state.operation) {
                MatrixOperation.Determinant -> ResultCard(
                    title = stringResource(R.string.matrix_determinant),
                ) {
                    if (state.determinant != null) {
                        Text(
                            text = formatMatrixNumber(state.determinant),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = palette.title,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.matrix_determinant_square_only),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
                MatrixOperation.Transpose -> state.transposed?.let { matrix ->
                    ResultCard(title = stringResource(R.string.matrix_transpose)) {
                        MatrixDisplay(matrix = matrix)
                    }
                }
                MatrixOperation.Inverse -> ResultCard(
                    title = stringResource(R.string.matrix_inverse),
                ) {
                    if (state.inverse != null) {
                        MatrixDisplay(matrix = state.inverse)
                    } else {
                        Text(
                            text = stringResource(R.string.matrix_impossible),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
                MatrixOperation.Multiply -> ResultCard(
                    title = stringResource(R.string.matrix_product),
                ) {
                    if (state.product != null) {
                        MatrixDisplay(matrix = state.product)
                    } else {
                        Text(
                            text = stringResource(R.string.matrix_incompatible),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixDimensions(
    rows: Int,
    cols: Int,
    onRowsChange: (Int) -> Unit,
    onColsChange: (Int) -> Unit,
) {
    DimensionRow(
        label = stringResource(R.string.matrix_rows),
        selected = rows,
        onSelect = onRowsChange,
    )
    DimensionRow(
        label = stringResource(R.string.matrix_cols),
        selected = cols,
        onSelect = onColsChange,
    )
}

@Composable
private fun MatrixInput(
    rows: Int,
    cols: Int,
    cells: List<String>,
    onCellChange: (Int, String) -> Unit,
    descriptionPrefix: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs)) {
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                repeat(cols) { col ->
                    val index = row * cols + col
                    val description = "$descriptionPrefix ${row + 1}, ${col + 1}"
                    OutlinedTextField(
                        value = cells.getOrElse(index) { "" },
                        onValueChange = { value -> onCellChange(index, value) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = description },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    content: @Composable () -> Unit,
) {
    EngineeringCard(title = title) {
        content()
    }
}

@Composable
private fun DimensionRow(
    label: String,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val palette = LocalKawaiiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
            modifier = Modifier.weight(1f),
        )
        listOf(2, 3).forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(value.toString()) },
            )
        }
    }
}

@Composable
private fun MatrixDisplay(matrix: NumericMatrix) {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
    ) {
        repeat(matrix.rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
            ) {
                repeat(matrix.cols) { col ->
                    Text(
                        text = formatMatrixNumber(matrix.values[row * matrix.cols + col]),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = palette.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun formatMatrixNumber(value: Double): String {
    if (value.isNearlyZero()) return "0"
    val rounded = round(value * 1_000_000.0) / 1_000_000.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString().trimEnd('0').trimEnd('.')
    }
}

@Composable
private fun matrixOperationLabel(operation: MatrixOperation): String = stringResource(
    when (operation) {
        MatrixOperation.Determinant -> R.string.matrix_op_det
        MatrixOperation.Transpose -> R.string.matrix_op_transpose
        MatrixOperation.Inverse -> R.string.matrix_op_inverse
        MatrixOperation.Multiply -> R.string.matrix_op_multiply
    },
)
