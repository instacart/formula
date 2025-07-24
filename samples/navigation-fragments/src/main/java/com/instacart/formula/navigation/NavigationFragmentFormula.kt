package com.instacart.formula.navigation

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxAction

class NavigationFragmentFormula(
    private val navigationStore: NavigationStore,
    private val onNavigationEffect: (NavigationEffect) -> Unit
) : Formula<NavigationFragmentFormula.Input, NavigationFragmentFormula.State, NavigationFragmentRenderModel>() {

    data class Input(
        val fragmentId: Int
    )

    data class State(
        val navigationState: NavigationState,
        val counter: Int = 0
    )

    override fun initialState(input: Input): State = State(
        navigationState = navigationStore.getCurrentState(),
        counter = 0
    )

    override fun Snapshot<Input, State>.evaluate(): Evaluation<NavigationFragmentRenderModel> {
        return Evaluation(
            output = NavigationFragmentRenderModel(
                fragmentId = input.fragmentId,
                counter = state.counter,
                backStackFragments = state.navigationState.navigationStack,
                onNavigateToNext = context.callback {
                    val nextFragmentId = state.navigationState.navigationStack.maxOrNull()?.plus(1) ?: 1
                    navigationStore.onEvent(NavigationEvent.NavigateToFragment(nextFragmentId))
                    transition(effect = {
                        onNavigationEffect(NavigationEffect.NavigateToFragment(nextFragmentId))
                    })
                },
                onNavigateBack = context.callback {
                    navigationStore.onEvent(NavigationEvent.NavigateBack)
                    transition(effect = {
                        onNavigationEffect(NavigationEffect.NavigateBack)
                    })
                },
                onIncrementCounter = { fragmentId ->
                    navigationStore.onEvent(NavigationEvent.IncrementCounter(fragmentId))
                }
            ),
            actions = context.actions {
                val stateAction = RxAction.fromObservable { navigationStore.state }
                stateAction.onEvent { navigationState ->
                    transition(state.copy(navigationState = navigationState))
                }

                val counterIncrementAction = RxAction.fromObservable {
                    navigationStore.counterIncrements.filter { it == input.fragmentId }
                }
                counterIncrementAction.onEvent {
                    transition(state.copy(counter = state.counter + 1))
                }
            }
        )
    }
}