package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class DelayFormula(
    private val delay: Duration,
) : Formula<Unit, DelayFormula.State, Int>() {
    data class State(val count: Int = 0)

    override fun initialState(input: Unit) = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Int> {
        return Evaluation(
            output = state.count,
            actions = context.actions {
                Action.fromFlow {
                    flow {
                        delay(delay)
                        emit(1)
                    }
                }.onEvent {
                    transition(state.copy(count = state.count + 1))
                }
            }
        )
    }
}