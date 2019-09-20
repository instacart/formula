package com.instacart.formula

import com.instacart.formula.utils.TestUtils
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

object ChildMessageTriggersEventTransitionInParent {
    class Service {
        private val relay: PublishRelay<Unit> = PublishRelay.create()

        fun trigger() {
            relay.accept(Unit)
        }

        fun serviceEvents(): Observable<Unit> {
            return relay
        }
    }

    data class TestRenderModel(
        val count: Int,
        val child: SideEffectFormula.RenderModel
    )

    fun formula(): Formula<Unit, Int, TestRenderModel> {
        val service = Service()
        val childFormula = SideEffectFormula.create(service::trigger)
        return TestUtils.create(0) { input, state, context ->
            Evaluation(
                renderModel = TestRenderModel(
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
