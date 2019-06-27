package com.instacart.formula.timer

class TimerRenderModel(
    val time: String,
    val onResetSelected: () -> Unit,
    val onStart: () -> Unit,
    val onClose: () -> Unit
)
