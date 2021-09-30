package com.instacart.formula.stopwatch

import com.instacart.formula.Listener

data class StopwatchRenderModel(
    val timePassed: String,
    val startStopButton: ButtonRenderModel,
    val resetButton: ButtonRenderModel
)

data class ButtonRenderModel(
    val text: String,
    val onSelected: Listener<Unit>
)
