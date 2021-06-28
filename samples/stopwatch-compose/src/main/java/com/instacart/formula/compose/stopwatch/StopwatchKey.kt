package com.instacart.formula.compose.stopwatch

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class StopwatchKey(override val tag: String = "stopwatch"): FragmentKey