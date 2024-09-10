package com.instacart.formula.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import com.instacart.formula.lint.WrongFormulaUsageDetector.Companion.issues
import org.junit.Test

class WrongFormulaUsageDetectorTest {
    private val TRANSITION_CONTEXT_STUB = """
        package com.instacart.formula

        interface TransitionContext<Input, State> {
            val input: Input
            val state: State
        }
    """.trimIndent()

    private val TRANSITION_STUB = """
        package com.instacart.formula

        fun interface Transition<Input, State, Event> {
            class Result<State>()
            
            open fun TransitionContext<Input, State>.toResult(event: Event): Result<State>
        }
    """.trimIndent()

    private val SNAPSHOT_STUB = """
        package com.instacart.formula

        interface Snapshot<Input, State> {
            val input: Input
            val state: State
            val context: FormulaContext<Input, State>
        }
    """.trimIndent()

    private val FORMULA_CONTEXT_STUB = """
        package com.instacart.formula

        class FormulaContext<Input, State> {
            open fun <Event> onEvent(transition: Transition<Input, State, Event>): (Event) -> Unit
            open fun <Event> onEvent(key: Any, transition: Transition<Input, State, Event>): (Event) -> Unit
            open fun callback(transition: Transition<Input, State, Unit>): () -> Unit 
            open fun callback(key: Any, transition: Transition<Input, State, Unit>): () -> Unit 
            open fun <Event> eventCallback(transition: Transition<Input, State, Event>): (Event) -> Unit 
            open fun <Event> eventCallback(key: Any, transition: Transition<Input, State, Event>): (Event) -> Unit 
            open fun <ChildOutput> child(child: IFormula<Unit, ChildOutput>): ChildOutput
            open fun <ChildInput, ChildOutput> child(formula: IFormula<ChildInput, ChildOutput>, input: ChildInput): ChildOutput
            open fun actions(init: ActionBuilder<Input, State>.() -> Unit): List<BoundStream<*>>
            open fun <Value> key(key: Any, create: () -> Value): Value
        }
    """.trimIndent()

    private val STREAM_BUILDER_STUB = """
        package com.instacart.formula
        
        class ActionBuilder<Input, State>() {
            open fun <Event> events(stream: Stream<Event>, transition: Transition<Input, State, Event>)
            open fun <Event> Stream<Event>.onEvent(transition: Transition<Input, State, Event>)
        }
    """.trimIndent()

    private val STATELESS_FORMULA_STUB = """
        package com.instacart.formula

        class StatelessFormula<Input, Output> {
            open fun Snapshot<Input, Unit>.evaluate(): Evaluation<Output>
            open fun key(input: Input): Any? = null
        }
    """.trimIndent()

    private val STREAM_STUB = """
        package com.instacart.formula
        
        class Stream<Event> {
            companion object {
                open fun onInit(): Stream<Unit>
            }
        }
    """.trimIndent()

    private fun run(vararg exampleCode: TestFile): TestLintResult {
        val array = arrayOf(
            kotlin(TRANSITION_CONTEXT_STUB),
            kotlin(TRANSITION_STUB),
            kotlin(STREAM_STUB),
            kotlin(STREAM_BUILDER_STUB),
            kotlin(FORMULA_CONTEXT_STUB),
            kotlin(SNAPSHOT_STUB),
            kotlin(STATELESS_FORMULA_STUB)
        ).plus(exampleCode)
        return lint()
            .files(*array)
            .issues(*issues)
            .skipTestModes(TestMode.SUPPRESSIBLE)
            .run()
    }

    @Test
    fun usingFormulaContextWithinTransitionContextIsIllegal() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |   fun Snapshot<Unit, Unit>.nestedCallback(): () -> Unit {
            |       return context.callback("with-key") {
            |           // Illegal call
            |           context.onEvent { none() }
            |           none()
            |       }
            |   }
            |}""".trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:7: Error: Cannot use FormulaContext within transition context [InvalidFormulaContextUsage]
                |           context.onEvent { none() }
                |                   ~~~~~~~~~~~~~~~~~~
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun passingSnapshotWithinTransitionContextIsIllegal() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |   fun Snapshot<Unit, Unit>.nestedCallback(): () -> Unit {
            |       return context.callback("with-key") {
            |           // Illegal call
            |           anotherCallback("laims fake param")
            |           none()
            |       }
            |   }
            |   
            |   fun Snapshot<Unit, Unit>.anotherCallback(param: String) {
            |       context.callback { none() } 
            |   }
            |}""".trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:7: Error: Using Snapshot within transition context is not allowed. Since anotherCallback takes Snapshot as a parameter, you cannot use this function with transition context. [InvalidFormulaContextUsage]
                |           anotherCallback("laims fake param")
                |           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun usingFormulaContextWithinStreamTransitionIsIllegal() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula : StatelessFormula<Unit, Unit>() {
            |   override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            |        return Evaluation(
            |            output = Unit,
            |            actions = context.actions {
            |                Stream.onInit().onEvent {
            |                    val callback = context.callback {
            |                        transition {}
            |                    }   
            |                    none()
            |                }
            |            }
            |        )
            |    }
            |}""".trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:9: Error: Cannot use FormulaContext within transition context [InvalidFormulaContextUsage]
                |                    val callback = context.callback {
                |                                           ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun callingFormulaContextOutsideOfEvaluateInDelegatedCall() {
        val delegatedCall = """
            package com.instacart.formula

            class DelegatedCall {

                fun illegalBehavior(context: FormulaContext<*, *>) {
                    context.callback { none() }
                }
            }
        """.trimIndent()

        val kotlinExample = """
            |package com.instacart.formula
            |
            |class ExampleFormula : StatelessFormula<Unit, Unit>() {
            |   override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
            |        return Evaluation(
            |            output = Unit,
            |            actions = context.actions {
            |                Stream.onInit().onEvent {
            |                    DelegatedCall().illegalBehavior(context)   
            |                    none()
            |                }
            |            }
            |        )
            |    }
            |}""".trimMargin()

        run(kotlin(delegatedCall), kotlin(kotlinExample)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:9: Error: Using FormulaContext within transition context is not allowed. Since illegalBehavior takes FormulaContext as a parameter, you cannot use this function with transition context. [InvalidFormulaContextUsage]
                |                    DelegatedCall().illegalBehavior(context)   
                |                                    ~~~~~~~~~~~~~~~~~~~~~~~~
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun usingFormulaContextInTransitionImplementationIsIllegal() {
        val kotlinExample = """
            |package com.instacart.formula
            |
            |class ExampleTransition(private val context: FormulaContext<Unit, Unit>): Transition<Unit, Unit, Unit> {
            |   override fun TransitionContext<Unit, Unit>.toResult(): Transition.Result<Unit> {
            |       // Illegal call
            |       context.callback { none() }
            |       return none()
            |    }
            |}""".trimMargin()

        run(kotlin(kotlinExample)).expect(
            """
                |src/com/instacart/formula/ExampleTransition.kt:6: Error: Cannot use FormulaContext within transition context [InvalidFormulaContextUsage]
                |       context.callback { none() }
                |               ~~~~~~~~~~~~~~~~~~~
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun usingSnapshotInTransitionImplementationIsIllegal() {
        val kotlinExample = """
            |package com.instacart.formula
            |
            |class ExampleTransition(private val snapshot: Snapshot<Unit, Unit>): Transition<Unit, Unit, Unit> {
            |   override fun TransitionContext<Unit, Unit>.toResult(): Transition.Result<Unit> {
            |       // Illegal call
            |       snapshot.context.callback { none() }
            |       return none()
            |    }
            |}""".trimMargin()

        run(kotlin(kotlinExample)).expect(
            """
                |src/com/instacart/formula/ExampleTransition.kt:6: Error: Cannot use FormulaContext within transition context [InvalidFormulaContextUsage]
                |       snapshot.context.callback { none() }
                |                        ~~~~~~~~~~~~~~~~~~~
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `event listener within for loop has to have unique key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        for (i in 0 until 10) {
            |            context.callback {
            |                none()
            |            }
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:6: Error: Key-less context.callback() call within an Iterable. Using a stable unique key allows Formula to re-use same callback across re-evaluation which enables proper equality checks. You should supply unique [key] to the context.callback() call. [KeylessFormulaCallbackWithinLoop]
                |            context.callback {
                |                    ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `event listener within while loop has to have unique key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        var i = 0
            |        while (i < 10) {
            |            context.callback {
            |                none()
            |            }
            |            i++            
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:7: Error: Key-less context.callback() call within an Iterable. Using a stable unique key allows Formula to re-use same callback across re-evaluation which enables proper equality checks. You should supply unique [key] to the context.callback() call. [KeylessFormulaCallbackWithinLoop]
                |            context.callback {
                |                    ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `event listener within do-while loop has to have unique key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        var i = 0
            |        do {
            |            context.callback {
            |                none()
            |            }
            |            i++            
            |        } while (i < 10)
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:7: Error: Key-less context.callback() call within an Iterable. Using a stable unique key allows Formula to re-use same callback across re-evaluation which enables proper equality checks. You should supply unique [key] to the context.callback() call. [KeylessFormulaCallbackWithinLoop]
                |            context.callback {
                |                    ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `event listener within foreach loop has to have unique key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        (0..10).forEach {
            |            context.callback {
            |                none()
            |            }
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:6: Error: Key-less context.callback() call within an Iterable. Using a stable unique key allows Formula to re-use same callback across re-evaluation which enables proper equality checks. You should supply unique [key] to the context.callback() call. [KeylessFormulaCallbackWithinLoop]
                |            context.callback {
                |                    ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `does not report KeylessFormulaCallbackWithinLoop when key-less callback is not inside Iterable loop`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackNotInLoop() {
            |        (0..10).forEach {
            |            // do nothing
            |        }
            |        context.callback {
            |            none()
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |No warnings.
            """.trimMargin()
        )
    }

    @Test
    fun `does not report KeylessFormulaCallbackWithinLoop when callback is inside Iterable loop and has key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        (0..10).forEach {
            |            context.callback(key = it) {
            |                none()
            |            }
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |No warnings.
            """.trimMargin()
        )
    }

    @Test
    fun `event listener within Iterable map function has to have unique key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        val callbacks = (0..10).map {
            |            context.callback {
            |                none()
            |            }
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:6: Error: Key-less context.callback() call within an Iterable. Using a stable unique key allows Formula to re-use same callback across re-evaluation which enables proper equality checks. You should supply unique [key] to the context.callback() call. [KeylessFormulaCallbackWithinLoop]
                |            context.callback {
                |                    ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `event listener within Iterable mapIndexed function has to have unique key`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        val callbacks = (0..10).mapIndexed { index, element ->
            |            context.callback {
            |                none()
            |            }
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |src/com/instacart/formula/ExampleFormula.kt:6: Error: Key-less context.callback() call within an Iterable. Using a stable unique key allows Formula to re-use same callback across re-evaluation which enables proper equality checks. You should supply unique [key] to the context.callback() call. [KeylessFormulaCallbackWithinLoop]
                |            context.callback {
                |                    ^
                |1 errors, 0 warnings
            """.trimMargin()
        )
    }

    @Test
    fun `does not report KeylessFormulaCallbackWithinLoop when key-less context#callback is enclosed in context#key call which in turn is enclosed in Iterable loop`() {
        val example = """
            |package com.instacart.formula
            |
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callbackInLoop() {
            |        (0..10).forEach {
            |            context.key(key = it) {
            |                context.callback {
            |                    none()
            |                }
            |            }
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |No warnings.
            """.trimMargin()
        )
    }

    @Test
    fun `does not report KeylessFormulaCallbackWithinLoop when key-less context#callback is enclosed in List#add call`() {
        val example = """
            |package com.instacart.formula
            |class ExampleFormula {
            |    data class Callback(
            |        val action: () -> Unit,
            |    )
            |
            |    fun Snapshot<Unit, Unit>.callbackNotInLoop() {
            |        val rows = mutableListOf<Any>()
            |        rows.add(
            |            Callback(
            |                action = context.callback {
            |                    none()
            |                }
            |            )
            |        )
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |No warnings.
            """.trimMargin()
        )
    }

    @Test
    fun `does not report KeylessFormulaCallbackWithinLoop when key-less context#callback is enclosed in List#apply call`() {
        val example = """
            |package com.instacart.formula
            |class ExampleFormula {
            |    data class Callback(
            |        val action: () -> Unit,
            |    )
            |
            |    fun Snapshot<Unit, Unit>.callbackNotInLoop() {
            |        mutableListOf<Any>().apply{
            |            this += Callback(
            |                action = context.callback {
            |                    none()
            |                }
            |            )
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |No warnings.
            """.trimMargin()
        )
    }

    /**
     * This test denotes [KeylessFormulaCallbackWithinLoop] check deficiency, rather than making sure things work properly.
     * We currently aren't able to detect [FormulaContext#callback] key-less usages that are enclosed in a loop that originates from another class.
     */
    @Test
    fun `does not report KeylessFormulaCallbackWithinLoop when key-less context#callback is enclosed in iterable that originates from another class`() {
        val example = """
            |package com.instacart.formula
            |class AnotherClass {
            |    fun Snapshot<Unit, Unit>.loop() {
            |        val exampleFormula = ExampleFormula()
            |        (0..10).forEach {
            |            with(exampleFormula) {
            |                callback(it)
            |            }
            |        }
            |    }
            |}
            |    
            |class ExampleFormula {
            |    fun Snapshot<Unit, Unit>.callback(index: Int) {
            |        context.callback {
            |            none()
            |        }
            |    }
            |}
        """.trimMargin()

        run(kotlin(example)).expect(
            """
                |No warnings.
            """.trimMargin()
        )
    }
}