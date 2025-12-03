package com.instacart.formula.compose.stopwatch

import com.instacart.formula.android.RouteKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class StopwatchKey(override val tag: String = "stopwatch"): RouteKey