package com.instacart.formula.navigation

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxAction

class NavigationFragmentFormula(
    private val navigationStore: NavigationStore,
) : Formula<NavigationFragmentFormula.Input, NavigationFragmentFormula.State, NavigationFragmentRenderModel>() {

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

    override fun Snapshot<Input, State>.evaluate(): Evaluation<NavigationFragmentRenderModel> {
        return Evaluation(
            output = NavigationFragmentRenderModel(
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
                RxAction.fromObservable { navigationStore.state }.onEvent { navigationState ->
                    transition(state.copy(navigationState = navigationState))
                }

                RxAction.fromObservable {
                    navigationStore.counterIncrements.filter { it == input.fragmentId }
                }.onEvent {
                    transition(state.copy(counter = state.counter + 1))
                }
            },
        )
    }
}