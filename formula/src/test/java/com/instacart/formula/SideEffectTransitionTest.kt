package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Test

class SideEffectTransitionTest {

    @Test fun `side effect triggered transition should work`() {
        TestFormula()
            .state(Unit)
            .test()
            .apply {
                values().last().start()
            }
            .apply {
                assertThat(values().last().countFromParent).isEqualTo(1)
            }

    }

    class Service {
        private val relay: PublishRelay<Unit> = PublishRelay.create()

        fun trigger() {
            relay.accept(Unit)
        }

        fun serviceEvents(): Observable<Unit> {
            return relay
        }
    }

    class TestFormula : Formula<Unit, Int, Unit, ChildFormula.RenderModel> {
        private val service = Service()
        private val childFormula = ChildFormula(service)

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int, Unit>
        ): Evaluation<ChildFormula.RenderModel> {
            val renderModel = context.child(childFormula, state)
            return Evaluation(
                renderModel = renderModel,
                updates = context.updates {
                    events("increment", service.serviceEvents()) {
                        transition(state + 1)
                    }
                }
            )
        }
    }

    class ChildFormula(
        private val service: Service
    ) : Formula<Int, Unit, Unit, ChildFormula.RenderModel> {

        class RenderModel(
            val countFromParent: Int,
            val start: () -> Unit
        )

        override fun initialState(input: Int): Unit = Unit

        override fun evaluate(input: Int, state: Unit, context: FormulaContext<Unit, Unit>): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    countFromParent = input,
                    start = context.callback {
                        sideEffect("starting service") {
                            service.trigger()
                        }
                    }
                )
            )
        }
    }
}
