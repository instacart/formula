package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.test.CountingInspector
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope

class PendingActionFormulaTerminatedOnActionInit(scope: CoroutineScope) {

    private val inspector = CountingInspector()
    private var observer: TestFormulaObserver<Unit, Int, ParentFormula>? = null
    private val actionFormula = ActionFormula(
        terminateAction = {
            observer?.dispose()
        }
    )

    private val formula = ParentFormula(actionFormula)
    val test = formula.test(scope, inspector = inspector).apply {
        observer = this
    }

    fun assertActionsStarted(expected: Int) = apply {
        inspector.assertActionsStarted(expected)
    }

    class ParentFormula(val actionFormula: ActionFormula): StatelessFormula<Unit, Int>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
            val output = context.child(actionFormula)
            return Evaluation(output)
        }
    }


    class ActionFormula(val terminateAction: () -> Unit) : Formula<Unit, Int, Int>() {

        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    RxAction.fromObservable {
                        terminateAction()
                        Observable.empty()
                    }.onEvent { none() }

                    // Pending actions
                    Action.onInit().onEvent {
                        transition(state + 1)
                    }

                    Action.onInit().onEvent {
                        transition(state + 1)
                    }

                    Action.onInit().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}