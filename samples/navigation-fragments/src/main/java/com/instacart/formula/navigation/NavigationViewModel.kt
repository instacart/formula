package com.instacart.formula.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.formula.runAsStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationViewModel : ViewModel() {

    private val navigationFormula = NavigationActivityFormula()

    private val _navigationEvents = MutableSharedFlow<NavigationAction>(
        extraBufferCapacity = 1
    )
    val navigationEvents: SharedFlow<NavigationAction> = _navigationEvents.asSharedFlow()

    val state: StateFlow<NavigationActivityFormula.Output> = navigationFormula.runAsStateFlow(
        viewModelScope,
        NavigationActivityFormula.Input(
            onNavigation = { action -> _navigationEvents.tryEmit(action) }
        )
    )
}
