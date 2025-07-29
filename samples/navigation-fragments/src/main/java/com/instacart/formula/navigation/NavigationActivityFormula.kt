package com.instacart.formula.navigation

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationActivityFormula : Formula<NavigationActivityFormula.Input, NavigationActivityFormula.State, NavigationActivityFormula.Output>() {

    data class Input(
        val onNavigation: (NavigationAction) -> Unit,
    )

    data class State(
        val store: NavigationStore = NavigationStore(),
        val navigationStack: List<Int> = listOf(0), // Start with fragment 0
    ) {
        class NavigationStore {
            private val _counterIncrements = MutableSharedFlow<Int>(
                replay = 0,
                extraBufferCapacity = Int.MAX_VALUE,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

            val counterIncrements: SharedFlow<Int> = _counterIncrements.asSharedFlow()

            fun incrementCounterFor(fragmentId: Int) {
                _counterIncrements.tryEmit(fragmentId)
            }
        }

        fun navigateToFragment(fragmentId: Int): State = copy(
            navigationStack = navigationStack + fragmentId,
        )

        fun navigateBack(): State = copy(
            navigationStack = if (navigationStack.size > 1) {
                navigationStack.dropLast(1)
            } else {
                navigationStack
            },
        )

        val currentFragmentId: Int get() = navigationStack.lastOrNull() ?: 0
    }

    data class Output(
        val currentFragmentId: Int,
        val navigationStack: List<Int>,
        val counterIncrements: SharedFlow<Int>,
        val onNavigateToNext: () -> Unit,
        val onNavigateBack: () -> Unit,
        val onIncrementCounter: (Int) -> Unit,
    )

    override fun initialState(input: Input): State = State()

    override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                currentFragmentId = state.currentFragmentId,
                navigationStack = state.navigationStack,
                counterIncrements = state.store.counterIncrements,
                onNavigateToNext = context.callback {
                    val nextFragmentId = state.navigationStack.maxOrNull()?.plus(1) ?: 1
                    val newState = state.navigateToFragment(nextFragmentId)
                    transition(newState) {
                        input.onNavigation(NavigationAction.NavigateToFragment(nextFragmentId))
                    }
                },
                onNavigateBack = context.callback {
                    val newState = state.navigateBack()
                    transition(newState) {
                        input.onNavigation(NavigationAction.NavigateBack)
                    }
                },
                onIncrementCounter = { fragmentId ->
                    state.store.incrementCounterFor(fragmentId)
                },
            )
        )
    }
}