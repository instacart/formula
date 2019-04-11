package com.instacart.formula.integration.test.auth

import com.instacart.formula.integration.FlowDeclaration
import io.reactivex.Flowable

class TestAuthFlowDeclaration : FlowDeclaration<TestAuthFlowDeclaration.Host>() {

    class Host(
        val onAuthCompleted: () -> Unit
    )

    override fun createFlow(): Flow<Host> {
        return Flow(
            bindings = listOf(
                bind { _, _: TestLoginFragmentContract ->
                    Flowable.empty<String>()
                },
                bind { _, _: TestSignUpFragmentContract ->
                    Flowable.empty<String>()
                }
            )
        )
    }
}
