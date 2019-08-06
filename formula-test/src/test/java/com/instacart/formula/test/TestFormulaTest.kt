package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import org.junit.Before
import org.junit.Test

class TestFormulaTest {
    lateinit var childFormula: ChildFormula
    lateinit var parentFormula: ParentFormula
    lateinit var subject: TestFormulaObserver<Unit, ParentFormula.RenderModel, ParentFormula>

    @Before fun setup() {
        childFormula = ChildFormula()
        parentFormula = ParentFormula(childFormula)
        subject = ParentFormula(childFormula = childFormula)
            .test {
                child(ChildFormula::class, ChildFormula.Button({}))
            }
    }

    @Test fun `send message using formula class`() {
        subject
            .childInput(ChildFormula::class) { onChangeName("my name") }
            .renderModel {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `send message using formula instance`() {
        subject
            .childInput(childFormula) { onChangeName("my name") }
            .renderModel {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `input passed to formula`() {
        subject.childInput(childFormula) {
            assertThat(name).isEqualTo("")
        }
    }

    class ParentFormula(
        private val childFormula: ChildFormula
    ) : Formula<Unit, ParentFormula.State, ParentFormula.RenderModel> {

        data class State(val name: String)

        data class RenderModel(
            val name: String,
            val button: ChildFormula.Button
        )

        override fun initialState(input: Unit): State = State(name = "")

        override fun evaluate(
            input: Unit,
            state: State,
            context: FormulaContext<State>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    name = state.name,
                    button = context
                        .child(childFormula)
                        .input {
                            ChildFormula.Input(
                                name = state.name,
                                onChangeName = context.eventCallback {
                                    state.copy(name = it).noMessages()
                                }
                            )
                        }
                )
            )
        }
    }

    class ChildFormula : StatelessFormula<ChildFormula.Input, ChildFormula.Button>() {

        data class Input(
            val name: String,
            val onChangeName: (newName: String) -> Unit
        )

        class Button(val onNameChanged: (String) -> Unit)

        override fun evaluate(input: Input, context: FormulaContext<Unit>): Evaluation<Button> {
            return Evaluation(
                renderModel = Button(onNameChanged = context.eventCallback {
                    message(input.onChangeName, input.name)
                })
            )
        }
    }
}
