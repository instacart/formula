package com.instacart.formula.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instacart.formula.runAsStateFlow
import com.instacart.formula.android.FragmentState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationViewModel(
    fragmentState: Flow<FragmentState>,
) : ViewModel() {

    private val navigationFormula = NavigationActivityFormula(fragmentState)

    private val _navigationEvents = MutableSharedFlow<NavigationAction>(
        replay = 1,
    )
    val navigationEvents: SharedFlow<NavigationAction> = _navigationEvents.asSharedFlow()

    val state: StateFlow<NavigationActivityFormula.Output> = navigationFormula.runAsStateFlow(
        viewModelScope,
        NavigationActivityFormula.Input(
            onNavigation = { action -> _navigationEvents.tryEmit(action) }
        )
    )
}

class NavigationViewModelFactory(
    private val fragmentState: Flow<FragmentState>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NavigationViewModel(fragmentState) as T
    }
}
