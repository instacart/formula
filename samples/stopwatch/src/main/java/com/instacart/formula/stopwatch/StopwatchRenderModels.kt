package com.instacart.formula.stopwatch

data class StopwatchRenderModel(
    val timePassed: String,
    val startStopButton: ButtonRenderModel,
    val resetButton: ButtonRenderModel
)

data class ButtonRenderModel(
    val text: String,
    val onSelected: () -> Unit
)
