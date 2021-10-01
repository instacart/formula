package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import org.junit.Before
import org.junit.Test

class TestFormulaTest {
    lateinit var childFormula: FakeChildFormula
    lateinit var parentFormula: ParentFormula
    lateinit var subject: TestFormulaObserver<Unit, ParentFormula.Output, ParentFormula>

    @Before fun setup() {
        childFormula = FakeChildFormula()
        parentFormula = ParentFormula(childFormula)
        subject = ParentFormula(childFormula = childFormula).test(Unit)
    }

    @Test fun `trigger listener using child input`() {
        subject
            .apply {
                childFormula.input { onChangeName("my name") }
            }
            .output {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `input passed to formula`() {
        childFormula.input {
            assertThat(name).isEqualTo("")
        }
    }

    class ParentFormula(
        private val childFormula: ChildFormula
    ) : Formula<Unit, ParentFormula.State, ParentFormula.Output> {

        data class State(val name: String)

        data class Output(
            val name: String,
            val button: ChildFormula.Button
        )

        override fun initialState(input: Unit): State = State(name = "")

        override fun evaluate(
            input: Unit,
            state: State,
            context: FormulaContext<State>
        ): Evaluation<Output> {
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
    }

    class FakeChildFormula : TestFormula<ChildFormula.Input, ChildFormula.Button>(), ChildFormula {
        override fun initialOutput() = ChildFormula.Button {}
    }
}
