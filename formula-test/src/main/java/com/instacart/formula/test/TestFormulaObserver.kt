package com.instacart.formula.test

import com.instacart.formula.Formula
import com.instacart.formula.FormulaRuntime
import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerFactory
import com.instacart.formula.internal.TransitionLock
import io.reactivex.Observable
import kotlin.reflect.KClass

class TestFormulaObserver<Input, Output, RenderModel>(
    private val testManagers: Map<KClass<*>, TestFormulaManager<*, *, *, *>>,
    private val input: Observable<Input>,
    private val formula: Formula<Input, *, Output, RenderModel>
) {

    class ManagerFactory(private val observer: TestFormulaObserver<*, *, *>) : FormulaManagerFactory {
        override fun <Input, State, Output, RenderModel> createChildManager(
            formula: Formula<Input, State, Output, RenderModel>,
            input: Input,
            transitionLock: TransitionLock
        ): FormulaManager<Input, State, Output, RenderModel> {
            return observer.findManager(formula::class)
        }
    }

    private val outputs = mutableListOf<Output>()

    private val observer = FormulaRuntime
        .start(
            input = input,
            formula = formula,
            onEvent = {
                outputs.add(it)
            },
            childManagerFactory = ManagerFactory(this)
        )
        .test()
        .assertNoErrors()

    fun <Input, State, Output, RenderModel> output(
        type: KClass<out Formula<Input, State, Output, RenderModel>>,
        output: Output
    ) = apply {
        findManager(type).output(output)
    }

    fun <Input, State, Output, RenderModel> output(
        formula: Formula<Input, State, Output, RenderModel>,
        output: Output
    ) = output(formula::class, output)

    fun values() = observer.values()

    inline fun renderModel(assert: (RenderModel) -> Unit) = apply {
        assert(values().last())
    }

    private fun <Input, State, Output, RenderModel> findManager(
        type: KClass<out Formula<Input, State, Output, RenderModel>>
    ): TestFormulaManager<Input, State, Output, RenderModel> {
        val manager = checkNotNull(testManagers[type]) {
            "missing manager registration for $type"
        }

        return manager as TestFormulaManager<Input, State, Output, RenderModel>
    }
}
