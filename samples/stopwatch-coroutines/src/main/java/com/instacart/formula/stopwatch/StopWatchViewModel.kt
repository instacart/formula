package com.instacart.formula.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.runAsStateFlow

class StopWatchViewModel(formula: StopwatchFormula = StopwatchFormula()) : ViewModel() {
    val viewOutputs = formula.runAsStateFlow(viewModelScope)
}