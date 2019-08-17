package com.instacart.formula.test

import com.instacart.formula.Formula
import com.instacart.formula.FormulaRuntime
import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerFactory
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.TransitionLock
import io.reactivex.Observable
import kotlin.reflect.KClass

class TestFormulaObserver<Input : Any, RenderModel : Any, FormulaT : Formula<Input, *, RenderModel>>(
    private val testManagers: Map<KClass<*>, TestFormulaManager<*, *, *>>,
    private val input: Observable<Input>,
    val formula: FormulaT,
    private val defaultToRealFormula: Boolean = true
) {

    class ManagerFactory(private val observer: TestFormulaObserver<*, *, *>) : FormulaManagerFactory {
        override fun <Input, State, RenderModel> createChildManager(
            formula: Formula<Input, State, RenderModel>,
            input: Input,
            transitionLock: TransitionLock
        ): FormulaManager<Input, State, RenderModel> {
            if (!observer.testManagers.containsKey(formula::class) && observer.defaultToRealFormula) {
                return FormulaManagerFactoryImpl().createChildManager(formula, input, transitionLock)
            }

            return observer.findManager(formula::class)
        }
    }

    private val observer = FormulaRuntime
        .start(
            input = input,
            formula = formula,
            childManagerFactory = ManagerFactory(this)
        )
        .test()
        .assertNoErrors()

    fun values(): List<RenderModel> {
        return observer.values()
    }

    inline fun <Input, State, RenderModel> childInput(
        childType: KClass<out Formula<Input, State, RenderModel>>,
        assert: Input.() -> Unit
    ) = apply {
        findManager(childType).lastInput().assert()
    }

    inline fun <Input, State, RenderModel> childInput(
        childFormula: Formula<Input, State, RenderModel>,
        assert: Input.() -> Unit
    ) = apply {
        findManager(childFormula::class).lastInput().assert()
    }

    inline fun renderModel(assert: RenderModel.() -> Unit) = apply {
        assert(values().last())
    }

    fun assertRenderModelCount(count: Int) = apply {
        val size = values().size
        assert(size == count) {
            "Expected: $count, was: $size"
        }
    }

    fun dispose() = apply {
        observer.dispose()
    }

    @PublishedApi
    internal fun <Input, State, RenderModel> findManager(
        type: KClass<out Formula<Input, State, RenderModel>>
    ): TestFormulaManager<Input, State, RenderModel> {
        val manager = checkNotNull(testManagers[type]) {
            "missing manager registration for $type"
        }

        @Suppress("UNCHECKED_CAST")
        return manager as TestFormulaManager<Input, State, RenderModel>
    }
}
