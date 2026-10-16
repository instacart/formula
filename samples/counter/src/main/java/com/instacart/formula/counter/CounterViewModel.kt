package com.instacart.formula.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.runAsStateFlow

class CounterViewModel : ViewModel() {
    val outputs = CounterFormula().runAsStateFlow(viewModelScope)
}
