package com.instacart.formula.integration

class TestAuthFlowIntegration : FlowIntegration<TestAuthFlowDeclaration.Input> {
    override val flowDeclaration = TestAuthFlowDeclaration()

    override fun input(): TestAuthFlowDeclaration.Input {
        return TestAuthFlowDeclaration.Input(
            onAuthCompleted = {
                // Todo
            }
        )
    }
}
