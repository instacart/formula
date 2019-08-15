package com.instacart.formula

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

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
            val child: SideEffectFormula.RenderModel
        )

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    count = state,
                    child = context.child(childFormula).input(Unit)
                ),
                updates = context.updates {
                    events(service.serviceEvents()) {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}
