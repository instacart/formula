package com.instacart.formula.navigation

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.android.FragmentState
import com.instacart.formula.navigation.NavigationActivityFormula.Input
import com.instacart.formula.navigation.NavigationActivityFormula.Output
import com.instacart.formula.navigation.NavigationActivityFormula.State
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationActivityFormula(
    private val fragmentState: Flow<FragmentState>,
) : Formula<Input, State, Output>() {

    data class Input(
        val onNavigation: (NavigationAction) -> Unit,
    )

    data class State(
        val countersStore: CountersStore = CountersStore(),
        val currentFragmentState: FragmentState = FragmentState(),
        val navigationStackFlow: MutableSharedFlow<List<Int>> = MutableSharedFlow(replay = 1, extraBufferCapacity = Int.MAX_VALUE),
    ) {
        class CountersStore {
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
    }

    data class Output(
        val navigationStack: SharedFlow<List<Int>>,
        val counterIncrements: SharedFlow<Int>,
        val onNavigateToNext: () -> Unit,
        val onNavigateBack: () -> Unit,
        val onIncrementCounter: (Int) -> Unit,
    )

    override fun initialState(input: Input): State = State()

    override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                navigationStack = state.navigationStackFlow,
                counterIncrements = state.countersStore.counterIncrements,
                onNavigateToNext = context.callback {
                    val nextFragmentId = state.currentFragmentState.navStack().maxOrNull()?.plus(1) ?: 1
                    transition {
                        input.onNavigation(NavigationAction.NavigateToFragment(nextFragmentId))
                    }
                },
                onNavigateBack = context.callback {
                    transition {
                        input.onNavigation(NavigationAction.NavigateBack)
                    }
                },
                onIncrementCounter = { fragmentId ->
                    state.countersStore.incrementCounterFor(fragmentId)
                },
            ),
            actions = context.actions {
                Action.onInit().onEvent {
                    transition {
                        // initial fragment
                        input.onNavigation(NavigationAction.NavigateToFragment(fragmentId = 0))
                    }
                }

                Action.fromFlow { fragmentState }.onEvent { newFragmentState ->
                    transition(state.copy(currentFragmentState = newFragmentState)) {
                        state.navigationStackFlow.tryEmit(newFragmentState.navStack())
                    }
                }
            },
        )
    }
}

private fun extractFragmentId(fragmentKey: Any?): Int {
    return when (fragmentKey) {
        is CounterFragmentKey -> fragmentKey.fragmentId
        else -> throw RuntimeException("Unexpected fragment key: $fragmentKey")
    }
}

private fun FragmentState.navStack(): List<Int> = activeIds.map { extractFragmentId(it.key) }