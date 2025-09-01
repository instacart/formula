package com.instacart.formula.navigation

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.navigation.CounterFragmentFormula.Input
import com.instacart.formula.navigation.CounterFragmentFormula.State
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter

class CounterFragmentFormula(
    private val dependencies: Dependencies,
) : Formula<Input, State, CounterFragmentRenderModel>() {

    interface Dependencies {
        val navigationStack: SharedFlow<List<Int>>
        val counterIncrements: SharedFlow<Int>
        val onNavigateToNext: () -> Unit
        val onNavigateBack: () -> Unit
        val onIncrementCounter: (Int) -> Unit
    }

    data class Input(
        val fragmentId: Int,
    )

    data class State(
        val navigationStack: List<Int> = emptyList(),
        val counter: Int = 0,
    )

    override fun initialState(input: Input): State = State()

    override fun Snapshot<Input, State>.evaluate(): Evaluation<CounterFragmentRenderModel> {
        return Evaluation(
            output = CounterFragmentRenderModel(
                fragmentId = input.fragmentId,
                counter = state.counter,
                backStackFragments = state.navigationStack,
                onNavigateToNext = context.callback {
                    transition {
                        dependencies.onNavigateToNext()
                    }
                },
                onNavigateBack = context.callback {
                    transition {
                        dependencies.onNavigateBack()
                    }
                },
                onIncrementCounter = context.onEvent { fragmentId ->
                    transition {
                        dependencies.onIncrementCounter(fragmentId)
                    }
                },
            ),
            actions = context.actions {
                Action.fromFlow {
                    dependencies.counterIncrements.filter { it == input.fragmentId }
                }.onEvent {
                    transition(state.copy(counter = state.counter + 1))
                }

                Action.fromFlow {
                    dependencies.navigationStack
                }.onEvent { stack ->
                    transition(state.copy(navigationStack = stack))
                }
            },
        )
    }
}