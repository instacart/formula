package com.instacart.formula.integration

class TestAuthFlowIntegration : FlowIntegration<Unit, TestAuthFlowDeclaration.Host>() {

    override val flowDeclaration = TestAuthFlowDeclaration()

    override fun createComponent(parentComponent: Unit): DisposableScope<TestAuthFlowDeclaration.Host> {
        val host = TestAuthFlowDeclaration.Host(
            onAuthCompleted = {
                // Todo
            }
        )

        return DisposableScope(host, {})
    }
}
