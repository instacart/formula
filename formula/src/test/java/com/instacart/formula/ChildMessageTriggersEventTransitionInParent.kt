package com.instacart.formula

import com.instacart.formula.rxjava3.RxStream
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

object ChildMessageTriggersEventTransitionInParent {
    fun formula() = TestFormula()

    class Service {
        private val relay: PublishRelay<Unit> = PublishRelay.create()

        fun trigger() {
            relay.accept(Unit)
        }

        fun serviceEvents(): Observable<Unit> {
            return relay
        }
    }

    class TestFormula : Formula<Unit, Int, TestFormula.RenderModel> {
        private val service = Service()
        private val childFormula = SideEffectFormula(service::trigger)

        class RenderModel(
            val count: Int,
            val child: SideEffectFormula.Output
        )

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<RenderModel> {
            return Evaluation(
                output = RenderModel(
                    count = state,
                    child = context.child(childFormula)
                ),
                updates = context.updates {
                    RxStream
                        .fromObservable { service.serviceEvents() }
                        .onEvent {
                            transition(state + 1)
                        }
                }
            )
        }
    }
}
