package com.instacart.formula.integration.test

import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FlowIntegration
import com.instacart.formula.integration.test.auth.TestAuthFlowDeclaration

class TestAuthFlowIntegration : FlowIntegration<TestAppComponent, TestAuthFlowDeclaration.Host>() {

    override val flowDeclaration = TestAuthFlowDeclaration()

    override fun createComponent(parentComponent: TestAppComponent): DisposableScope<TestAuthFlowDeclaration.Host> {
        val host = TestAuthFlowDeclaration.Host(
            onAuthCompleted = {
                // Todo
            }
        )

        return DisposableScope(host, {})
    }
}
