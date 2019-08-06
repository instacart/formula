package com.instacart.formula.test

import com.instacart.formula.Formula
import kotlin.reflect.KClass

class ChildFormulaRegistryBuilder {
    internal val testManagers: MutableMap<KClass<*>, TestFormulaManager<*, *, *>> = mutableMapOf()

    /**
     * Registers a child [RenderModel] that will always be returned.
     */
    fun <Input, State, RenderModel> child(
        formula: KClass<out Formula<Input, State, RenderModel>>,
        renderModel: RenderModel
    ) {
        testManagers[formula] = TestFormulaManager<Input, State, RenderModel>(renderModel)
    }

    /**
     * Registers a child [RenderModel] that will always be returned.
     */
    fun <Input, State, RenderModel> child(
        formula: Formula<Input, State, RenderModel>,
        renderModel: RenderModel
    ) = child(formula::class, renderModel)
}
