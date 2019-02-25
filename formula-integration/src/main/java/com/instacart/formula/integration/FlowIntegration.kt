package com.instacart.formula.integration

/**
 * Defines integration of a flow.
 *
 * [Input] - Input passed to flow declaration to create a binding.
 */
interface FlowIntegration<Input> {

    val flowDeclaration: FlowDeclaration<Input, Unit, *>

    fun input(): Input

    fun binding() = flowDeclaration.createBinding(input())
}
