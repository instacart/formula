package com.instacart.formula.navigation

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter

class CounterFragmentFormula : Formula<CounterFragmentFormula.Input, CounterFragmentFormula.State, CounterFragmentRenderModel>() {

    data class Input(
        val fragmentId: Int,
        val navigationStack: List<Int>,
        val counterIncrements: SharedFlow<Int>,
        val onNavigateToNext: () -> Unit,
        val onNavigateBack: () -> Unit,
        val onIncrementCounter: (Int) -> Unit,
    )

    data class State(
        val counter: Int = 0,
    )

    override fun initialState(input: Input): State = State()

    override fun Snapshot<Input, State>.evaluate(): Evaluation<CounterFragmentRenderModel> {
        return Evaluation(
            output = CounterFragmentRenderModel(
                fragmentId = input.fragmentId,
                counter = state.counter,
                backStackFragments = input.navigationStack,
                onNavigateToNext = context.callback {
                    transition {
                        input.onNavigateToNext()
                    }
                },
                onNavigateBack = context.callback {
                    transition {
                        input.onNavigateBack()
                    }
                },
                onIncrementCounter = { fragmentId ->
                    input.onIncrementCounter(fragmentId)
                },
            ),
            actions = context.actions {
                Action.fromFlow {
                    input.counterIncrements.filter { it == input.fragmentId }
                }.onEvent {
                    transition(state.copy(counter = state.counter + 1))
                }
            },
        )
    }
}