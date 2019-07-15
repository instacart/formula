package com.instacart.formula

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

object SideEffectTriggersParentTransition {
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

    class TestFormula : Formula<Unit, Int, Unit, TestFormula.RenderModel> {
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
            context: FormulaContext<Int, Unit>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    count = state,
                    child = context.child(childFormula, Unit)
                ),
                updates = context.updates {
                    events("increment", service.serviceEvents()) {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}
