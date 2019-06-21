package com.instacart.formula

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object ProcessorFormulaRxRuntime {
    fun <Input, State, Effect, RenderModel> start(
        input: Input,
        formula: ProcessorFormula<Input, State, Effect, RenderModel>,
        onEffect: (Effect) -> Unit
    ): Observable<RenderModel> {
        val relay = PublishSubject.create<Unit>()

        val processorManager: ProcessorManager<State, Effect> = ProcessorManager(
            state = formula.initialState(input),
            onTransition = {
                if (it != null) {
                    onEffect(it)
                }

                relay.onNext(Unit)
            }
        )

        return relay
            .startWith(Unit)
            .map {
                processorManager.process(formula, input)
            }
            .map { it.renderModel }
            .distinctUntilChanged()
            .doFinally {
                processorManager.terminate()
            }
    }
}
