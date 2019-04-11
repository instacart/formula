package com.instacart.formula.integration.test

import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FlowIntegration
import com.instacart.formula.integration.test.loggedin.TestLoggedInFlowDeclaration

class TestLoggedInFlowIntegration : FlowIntegration<TestAppComponent, TestLoggedInFlowDeclaration.Component>() {

    override val flowDeclaration = TestLoggedInFlowDeclaration()

    override fun createComponent(parentComponent: TestAppComponent): DisposableScope<TestLoggedInFlowDeclaration.Component> {
        val host = TestLoggedInFlowDeclaration.Component()
        return DisposableScope(host, {})
    }
}
