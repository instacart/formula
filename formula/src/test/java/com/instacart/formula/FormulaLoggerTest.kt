package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.rxjava3.toObservable
import com.jakewharton.rxrelay3.BehaviorRelay
import org.junit.Test

class FormulaLoggerTest {

    @Test
    fun `logging single formula lifecycle`() {
        val logger = RecordingLogger()
        StartStopFormula()
            .toObservable(logger = logger)
            .test()
            .apply {
                values().last().startListening()
            }
            .dispose()

        assertThat(logger.events()).containsExactly(
            "StartStopFormula:initialState",
            "StartStopFormula:evaluate:run",
            "StartStopFormula:execution:started",
            "StartStopFormula:execution:finished",
            "StartStopFormula:transition:needsEvaluation:true",
            "StartStopFormula:evaluate:run",
            "StartStopFormula:execution:started",
            "StartStopFormula:stream:start com.instacart.formula.IncrementRelay-stream-inlined-fromObservable-1",
            "StartStopFormula:execution:finished",
            "StartStopFormula:terminating",
            "StartStopFormula:stream:terminate com.instacart.formula.IncrementRelay-stream-inlined-fromObservable-1"
        ).inOrder()
    }

    @Test
    fun `logging formula with child formulas`() {
        val logger = RecordingLogger()
        val inputs = BehaviorRelay.createDefault(2)
        ParentFormula().toObservable(inputs, logger = logger)
            .test()
            .apply { inputs.accept(1) }
            .dispose()


        assertThat(logger.events()).containsExactly(
            "ParentFormula:initialState",
            "ParentFormula:evaluate:run",
            "|- ChildFormula-1:initialState",
            "|- ChildFormula-1:evaluate:run",
            "|- ChildFormula-2:initialState",
            "|- ChildFormula-2:evaluate:run",
            "ParentFormula:execution:started",
            "ParentFormula:execution:finished",
            "ParentFormula:onInputChanged",
            "ParentFormula:evaluate:run",
            "|- ChildFormula-1:evaluate:skip (no changes, returning cached output)",
            "ParentFormula:execution:started",
            "|- ChildFormula-2:terminating",
            "ParentFormula:execution:finished",
            "|- ChildFormula-1:terminating",
            "ParentFormula:terminating",
        ).inOrder()
    }

    @Test
    fun `transition during execution phase`() {
        val logger = RecordingLogger()
        StreamInitFormula().toObservable(logger = logger)
            .test()

        assertThat(logger.events()).containsExactly(

            "StreamInitFormula:initialState",
            "StreamInitFormula:evaluate:run",
            "StreamInitFormula:execution:started",
            "StreamInitFormula:stream:start com.instacart.formula.StartMessageStream",
            "StreamInitFormula:transition:needsEvaluation:true",
            "StreamInitFormula:execution:stopped (finished early due to a transition)",
            "StreamInitFormula:evaluate:run",
            "StreamInitFormula:execution:started",
            "StreamInitFormula:stream:skip (already running com.instacart.formula.StartMessageStream)",
            "StreamInitFormula:execution:finished"
        ).inOrder()
    }

    class ParentFormula : StatelessFormula<Int, List<String>>() {
        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<List<String>> {
            val outputs = (1..input).map {
                context.child(ChildFormula(), it)
            }
            return Evaluation(
                output = outputs
            )
        }
    }

    class ChildFormula : StatelessFormula<Int, String>() {
        override fun key(input: Int): Any = input

        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<String> {
            return Evaluation(
                output = "Value: $input"
            )
        }
    }

    class StreamInitFormula : Formula<Unit, Int, Int> {
        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Int> {
            return Evaluation(
                output = state,
                updates = context.updates {
                    Stream.onInit().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }

    class RecordingLogger : Logger {
        private val events = mutableListOf<String>()

        override fun logEvent(event: String) {
            events.add(event)
        }

        fun events(): List<String> = events
    }
}