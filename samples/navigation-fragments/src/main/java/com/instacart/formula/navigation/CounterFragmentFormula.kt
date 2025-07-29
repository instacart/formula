package com.instacart.formula.navigation

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.flow.filter

class CounterFragmentFormula(
    private val navigationStore: NavigationStore,
) : Formula<CounterFragmentFormula.Input, CounterFragmentFormula.State, CounterFragmentRenderModel>() {

    data class Input(
        val fragmentId: Int,
        val onNavigationEffect: (NavigationEffect) -> Unit,
    )

    data class State(
        val navigationState: NavigationState,
        val counter: Int = 0,
    )

    override fun initialState(input: Input): State = State(
        navigationState = navigationStore.getCurrentState(),
        counter = 0,
    )

    override fun Snapshot<Input, State>.evaluate(): Evaluation<CounterFragmentRenderModel> {
        return Evaluation(
            output = CounterFragmentRenderModel(
                fragmentId = input.fragmentId,
                counter = state.counter,
                backStackFragments = state.navigationState.navigationStack,
                onNavigateToNext = context.callback {
                    transition {
                        val nextFragmentId = state.navigationState.navigationStack.maxOrNull()?.plus(1) ?: 1
                        navigationStore.onEvent(NavigationEvent.NavigateToFragment(nextFragmentId))
                        input.onNavigationEffect(NavigationEffect.NavigateToFragment(nextFragmentId))
                    }
                },
                onNavigateBack = context.callback {
                    transition {
                        navigationStore.onEvent(NavigationEvent.NavigateBack)
                        input.onNavigationEffect(NavigationEffect.NavigateBack)
                    }
                },
                onIncrementCounter = { fragmentId ->
                    navigationStore.onEvent(NavigationEvent.IncrementCounter(fragmentId))
                },
            ),
            actions = context.actions {
                Action.fromFlow { navigationStore.state }.onEvent { navigationState ->
                    transition(state.copy(navigationState = navigationState))
                }

                Action.fromFlow {
                    navigationStore.counterIncrements.filter { it == input.fragmentId }
                }.onEvent {
                    transition(state.copy(counter = state.counter + 1))
                }
            },
        )
    }
}