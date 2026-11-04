package com.calculino.app.ui.geometry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.calculino.app.domain.geometry.GeometryCalculator
import com.calculino.app.domain.geometry.GeometryMeasurements
import com.calculino.app.domain.geometry.GeometryShape
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GeometryUiState(
    val shape: GeometryShape = GeometryShape.Circle,
    val circleRadius: Float = 3f,
    val rectangleWidth: Float = 4f,
    val rectangleHeight: Float = 3f,
    val triangleBase: Float = 4f,
    val triangleHeight: Float = 3f,
) {
    val measurements: GeometryMeasurements
        get() = when (shape) {
            GeometryShape.Circle -> GeometryCalculator.circle(circleRadius.toDouble())
            GeometryShape.Rectangle -> GeometryCalculator.rectangle(
                rectangleWidth.toDouble(),
                rectangleHeight.toDouble(),
            )
            GeometryShape.RightTriangle -> GeometryCalculator.rightTriangle(
                triangleBase.toDouble(),
                triangleHeight.toDouble(),
            )
        }
}

class GeometryViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GeometryUiState(
            shape = savedStateHandle.get<String>(KEY_SHAPE)
                ?.let { saved -> GeometryShape.entries.firstOrNull { it.name == saved } }
                ?: GeometryShape.Circle,
            circleRadius = savedStateHandle.get<Float>(KEY_RADIUS) ?: 3f,
            rectangleWidth = savedStateHandle.get<Float>(KEY_RECT_WIDTH) ?: 4f,
            rectangleHeight = savedStateHandle.get<Float>(KEY_RECT_HEIGHT) ?: 3f,
            triangleBase = savedStateHandle.get<Float>(KEY_TRI_BASE) ?: 4f,
            triangleHeight = savedStateHandle.get<Float>(KEY_TRI_HEIGHT) ?: 3f,
        ),
    )
    val uiState = _uiState.asStateFlow()

    fun onShapeChange(shape: GeometryShape) {
        update(_uiState.value.copy(shape = shape))
    }

    fun onCircleRadiusChange(value: Float) {
        update(_uiState.value.copy(circleRadius = value))
    }

    fun onRectangleWidthChange(value: Float) {
        update(_uiState.value.copy(rectangleWidth = value))
    }

    fun onRectangleHeightChange(value: Float) {
        update(_uiState.value.copy(rectangleHeight = value))
    }

    fun onTriangleBaseChange(value: Float) {
        update(_uiState.value.copy(triangleBase = value))
    }

    fun onTriangleHeightChange(value: Float) {
        update(_uiState.value.copy(triangleHeight = value))
    }

    private fun update(state: GeometryUiState) {
        _uiState.value = state
        savedStateHandle[KEY_SHAPE] = state.shape.name
        savedStateHandle[KEY_RADIUS] = state.circleRadius
        savedStateHandle[KEY_RECT_WIDTH] = state.rectangleWidth
        savedStateHandle[KEY_RECT_HEIGHT] = state.rectangleHeight
        savedStateHandle[KEY_TRI_BASE] = state.triangleBase
        savedStateHandle[KEY_TRI_HEIGHT] = state.triangleHeight
    }

    private companion object {
        const val KEY_SHAPE = "shape"
        const val KEY_RADIUS = "radius"
        const val KEY_RECT_WIDTH = "rect_width"
        const val KEY_RECT_HEIGHT = "rect_height"
        const val KEY_TRI_BASE = "tri_base"
        const val KEY_TRI_HEIGHT = "tri_height"
    }
}
