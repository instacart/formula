package com.instacart.formula.integration

import io.reactivex.Flowable

class TestAuthFlowDeclaration : FlowDeclaration<TestAuthFlowDeclaration.Input, Unit, Unit>() {
    class Input(
        val onAuthCompleted: () -> Unit
    )

    override fun createFlow(input: Input): Flow<Unit, Unit> {
        return Flow(
            flowComponentFactory = {
                DisposableScope(Unit) {}
            },
            childrenBindings = listOf(
                bind(TestLoginFragmentContract::class) { _, _ ->
                    Flowable.empty()
                },
                bind(TestSignUpFragmentContract::class) { _, _ ->
                    Flowable.empty()
                }
            )
        )
    }
}
