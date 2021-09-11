package com.instacart.formula.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.flow.*

class StopWatchViewModel(private val formula: StopwatchFormula = StopwatchFormula()) : ViewModel() {

    val rendererStream by lazy {
        formula.toFlow() //Let's imagine this flow uses a really expensive resource (i.e. Connectivity monitoring)
            //Cannot use .stateIn() since we do not have initial StopwatchRenderModel
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
            .distinctUntilChanged()
    }
}