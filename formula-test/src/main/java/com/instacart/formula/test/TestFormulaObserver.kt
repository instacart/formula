package com.instacart.formula.test

import com.instacart.formula.Formula
import com.instacart.formula.FormulaRuntime
import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerFactory
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.TransitionLock
import io.reactivex.Observable
import kotlin.reflect.KClass

class TestFormulaObserver<Input : Any, Output, RenderModel, FormulaT : Formula<Input, *, Output, RenderModel>>(
    private val testManagers: Map<KClass<*>, TestFormulaManager<*, *, *, *>>,
    private val input: Observable<Input>,
    val formula: FormulaT,
    private val defaultToRealFormula: Boolean = true
) {

    class ManagerFactory(private val observer: TestFormulaObserver<*, *, *, *>) : FormulaManagerFactory {
        override fun <Input, State, Output, RenderModel> createChildManager(
            formula: Formula<Input, State, Output, RenderModel>,
            input: Input,
            transitionLock: TransitionLock
        ): FormulaManager<Input, State, Output, RenderModel> {
            if (!observer.testManagers.containsKey(formula::class) && observer.defaultToRealFormula) {
                return FormulaManagerFactoryImpl().createChildManager(formula, input, transitionLock)
            }

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

    fun values(): List<RenderModel> {
        return observer.values()
    }

    inline fun <Input, State, Output, RenderModel> childInput(
        childType: KClass<out Formula<Input, State, Output, RenderModel>>,
        assert: Input.() -> Unit
    ) = apply {
        findManager(childType).lastInput().assert()
    }

    inline fun <Input, State, Output, RenderModel> childInput(
        childFormula: Formula<Input, State, Output, RenderModel>,
        assert: Input.() -> Unit
    ) = apply {
        findManager(childFormula::class).lastInput().assert()
    }

    inline fun renderModel(assert: RenderModel.() -> Unit) = apply {
        assert(values().last())
    }

    fun assertRenderModelCount(count: Int) = apply {
        assert(values().size == count)
    }

    fun outputs(): List<Output> = outputs

    fun assertOutputCount(count: Int) = apply {
        assert(outputs.size == count)
    }

    inline fun outputs(assert: List<Output>.() -> Unit) = apply {
        assert(outputs())
    }

    @PublishedApi
    internal fun <Input, State, Output, RenderModel> findManager(
        type: KClass<out Formula<Input, State, Output, RenderModel>>
    ): TestFormulaManager<Input, State, Output, RenderModel> {
        val manager = checkNotNull(testManagers[type]) {
            "missing manager registration for $type"
        }

        @Suppress("UNCHECKED_CAST")
        return manager as TestFormulaManager<Input, State, Output, RenderModel>
    }
}
