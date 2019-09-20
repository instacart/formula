package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.utils.TestUtils
import org.junit.Before
import org.junit.Test

class TestFormulaTest {
    lateinit var childFormula: Formula<Child.Input, Unit, Child.Button>
    lateinit var subject: TestFormulaObserver<Unit, Parent.RenderModel, *>

    @Before
    fun setup() {
        childFormula = Child.create()
        subject = Parent.formula(childFormula)
            .test {
                child(childFormula::class, Child.Button({}))
            }
    }

    @Test
    fun `send message using formula class`() {
        subject
            .childInput(childFormula::class) { onChangeName("my name") }
            .renderModel {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test
    fun `send message using formula instance`() {
        subject
            .childInput(childFormula) { onChangeName("my name") }
            .renderModel {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test
    fun `input passed to formula`() {
        subject.childInput(childFormula) {
            assertThat(name).isEqualTo("")
        }
    }

    object Parent {
        data class State(val name: String)

        data class RenderModel(
            val name: String,
            val button: Child.Button
        )

        fun formula(
            child: Formula<Child.Input, *, Child.Button>
        ) = TestUtils.create(State(name = "")) { state, context ->
            Evaluation(
                renderModel = RenderModel(
                    name = state.name,
                    button = context
                        .child(child)
                        .input {
                            Child.Input(
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

    object Child {
        data class Input(
            val name: String,
            val onChangeName: (newName: String) -> Unit
        )

        data class Button(val onNameChanged: (String) -> Unit)

        fun create() = TestUtils.stateless { input: Input, context ->
            Evaluation(
                renderModel = Button(onNameChanged = context.eventCallback {
                    message(input.onChangeName, input.name)
                })
            )
        }
    }
}
