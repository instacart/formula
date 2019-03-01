package com.instacart.formula.counter

import com.instacart.formula.Reducers
import com.instacart.formula.RenderLoop
import com.instacart.formula.RenderLoopFormula
import com.instacart.formula.RenderModelGenerator
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

class CounterRenderFormula : RenderLoopFormula<Unit, Int, Unit, CounterRenderModel> {

    override fun createRenderLoop(input: Unit): RenderLoop<Int, Unit, CounterRenderModel> {

        val incrementRelay = PublishRelay.create<Unit>()
        val decrementRelay = PublishRelay.create<Unit>()

        val modifications = Modifications()
        val reducers = Observable.merge(
            incrementRelay.map { modifications.increment() },
            decrementRelay.map { modifications.decrement() }
        )

        return RenderLoop(
            initialState = 0,
            reducers = reducers.toFlowable(BackpressureStrategy.BUFFER),
            renderModelGenerator = RenderModelGenerator.create { currentCount ->
                CounterRenderModel(
                    count = "Count: $currentCount",
                    onDecrement = {
                        decrementRelay.accept(Unit)
                    },
                    onIncrement = {
                        incrementRelay.accept(Unit)
                    }
                )
            }
        )
    }

    class Modifications : Reducers<Int, Unit>() {
        fun increment() = withoutEffects {
            it + 1
        }

        fun decrement() = withoutEffects {
            it - 1
        }
    }
}
