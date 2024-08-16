package com.instacart.formula.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot

class TestFormulaRobot {

    private val childFormula: FakeChildFormula = FakeChildFormula()
    private val parentFormula: ParentFormula = ParentFormula(childFormula)
    private val observer = parentFormula.test()


    fun start() = apply {
        observer.input(Unit)
    }

    fun withTestFormula(assertion: TestFormula<ChildFormula.Input, ChildFormula.Button>.() -> Unit) = apply {
        childFormula.implementation.assertion()
    }

    fun assertOutput(on: ParentFormula.Output.() -> Unit) = apply {
        observer.output(on)
    }

    class ParentFormula(
        private val childFormula: ChildFormula
    ) : Formula<Unit, ParentFormula.State, ParentFormula.Output>() {

        data class State(val name: String)

        data class Output(
            val name: String,
            val button: ChildFormula.Button
        )

        override fun initialState(input: Unit): State = State(name = "")

        override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    name = state.name,
                    button = context.child(childFormula, ChildFormula.Input(
                        name = state.name,
                        onChangeName = context.onEvent<String> {
                            transition(state.copy(name = it))
                        }
                    ))
                )
            )
        }
    }

    interface ChildFormula : IFormula<ChildFormula.Input, ChildFormula.Button> {

        data class Input(
            val name: String,
            val onChangeName: (newName: String) -> Unit
        )

        class Button(val onNameChanged: (String) -> Unit)

        override fun key(input: Input): Any? = "child-key"
    }

    class FakeChildFormula : ChildFormula {
        override val implementation = testFormula(
            initialOutput = ChildFormula.Button {}
        )
    }
}