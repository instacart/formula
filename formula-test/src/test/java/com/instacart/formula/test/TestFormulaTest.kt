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
    lateinit var subject: TestFormulaObserver<Unit, Unit, ParentFormula.RenderModel, ParentFormula>

    @Before fun setup() {
        childFormula = ChildFormula()
        parentFormula = ParentFormula(childFormula)
        subject = ParentFormula(childFormula = childFormula)
            .test {
                child(ChildFormula::class, ChildFormula.Button({}))
            }
    }

    @Test fun `output using formula class`() {
        subject
            .output(ChildFormula::class, ChildFormula.ChangeNameTo("my name"))
            .renderModel {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `output using formula`() {
        subject
            .output(childFormula, ChildFormula.ChangeNameTo("my name"))
            .renderModel {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `input passed to formula`() {
        subject.childInput(childFormula) {
            assertThat(this).isEqualTo("")
        }
    }

    class ParentFormula(
        private val childFormula: ChildFormula
    ) : Formula<Unit, ParentFormula.State, Unit, ParentFormula.RenderModel> {

        data class State(val name: String)

        data class RenderModel(
            val name: String,
            val button: ChildFormula.Button
        )

        override fun initialState(input: Unit): State = State(name = "")

        override fun evaluate(
            input: Unit,
            state: State,
            context: FormulaContext<State, Unit>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    name = state.name,
                    button = context.child(childFormula)
                        .onOutput {
                            transition(state.copy(name = it.newName))
                        }
                        .input(state.name)
                )
            )
        }
    }

    class ChildFormula : StatelessFormula<String, ChildFormula.ChangeNameTo, ChildFormula.Button>() {
        class ChangeNameTo(val newName: String)

        class Button(val onNameChanged: (String) -> Unit)

        override fun evaluate(input: String, context: FormulaContext<Unit, ChangeNameTo>): Evaluation<Button> {
            return Evaluation(
                renderModel = Button(onNameChanged = context.eventCallback("change name") {
                    output(ChangeNameTo(it))
                })
            )
        }
    }
}
