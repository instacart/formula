package com.instacart.formula.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn

class StopWatchViewModel(private val formula: StopwatchFormula = StopwatchFormula()) : ViewModel() {

    val renderModelFlow by lazy {
        formula.toFlow().shareIn(viewModelScope, SharingStarted.Eagerly, 1)
    }
}