package com.instacart.formula.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.runAsStateFlow

class StopwatchViewModel : ViewModel() {
    val outputs = StopwatchFormula().runAsStateFlow(viewModelScope)
}
