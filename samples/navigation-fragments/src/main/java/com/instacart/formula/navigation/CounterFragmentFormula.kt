package com.instacart.formula.navigation

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.navigation.CounterFragmentFormula.Input
import com.instacart.formula.navigation.CounterFragmentFormula.State

class CounterFragmentFormula(
    private val counterStore: CounterStore,
    private val counterRouter: CounterRouter,
) : Formula<Input, State, CounterFragmentRenderModel>() {

    data class Input(
        val counterIndex: Int,
    )

    data class State(
        val navigationStack: List<Int> = emptyList(),
        val counter: Int = 0,
    )

    override fun initialState(input: Input): State = State()

    override fun Snapshot<Input, State>.evaluate(): Evaluation<CounterFragmentRenderModel> {
        return Evaluation(
            output = CounterFragmentRenderModel(
                fragmentId = input.counterIndex,
                counter = state.counter,
                backStackFragments = state.navigationStack,
                onNavigateToNext = context.callback {
                    transition {
                        counterRouter.onNavigateToNext(input.counterIndex + 1)
                    }
                },
                onNavigateBack = context.callback {
                    transition {
                        counterRouter.onNavigateBack()
                    }
                },
                onIncrementCounter = context.onEvent { fragmentId ->
                    transition {
                        counterStore.incrementCounterFor(fragmentId)
                    }
                },
            ),
            actions = context.actions {
                Action.fromFlow {
                    counterStore.counterIncrements(input.counterIndex)
                }.onEvent {
                    transition(state.copy(counter = state.counter + 1))
                }

                Action.fromFlow {
                    counterStore.counterStack()
                }.onEvent { stack ->
                    transition(state.copy(navigationStack = stack))
                }
            },
        )
    }
}