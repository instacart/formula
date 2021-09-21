package com.instacart.formula

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.Try
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.streams.EmptyStream
import com.instacart.formula.subjects.DelegateFormula
import com.instacart.formula.subjects.EmptyFormula
import com.instacart.formula.subjects.HasChildFormula
import com.instacart.formula.subjects.EventFormula
import com.instacart.formula.subjects.StateTransitionTimingFormula
import com.instacart.formula.subjects.StreamInputFormula
import com.instacart.formula.test.CoroutinesTestableRuntime
import com.instacart.formula.test.RxJavaTestableRuntime
import com.instacart.formula.test.TestCallback
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.TestableRuntime
import com.instacart.formula.test.test
import com.instacart.formula.tests.EmitErrorTest
import io.reactivex.rxjava3.core.Observable
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FormulaRuntimeTest(val runtime: TestableRuntime, val name: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun runtimes() = listOf(
            arrayOf(RxJavaTestableRuntime, "RxJava"),
            arrayOf(CoroutinesTestableRuntime, "Coroutines"),
        )
    }

    @get:Rule
    val rule = RuleChain.outerRule(TestName()).around(runtime.rule)

    @Test
    fun `transition effects are performed after state is updated`() {
        // TODO: not sure if this test is very clear.
        val formula = StateTransitionTimingFormula(runtime)
        val expectedStates = listOf(
            StateTransitionTimingFormula.State.INTERNAL,
            StateTransitionTimingFormula.State.EXTERNAL
        )

        runtime.test(formula, Unit).output { onStateTransition() }.output {
            assertThat(events).isEqualTo(expectedStates)
        }
    }

    @Test
    fun `multiple event updates`() {
        runtime.test(StartStopFormula(runtime), Unit)
            .output { startListening() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply {
                val expected = listOf(0, 0, 1, 2, 3)
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test
    fun `no state changes after event stream is removed`() {
        runtime.test(StartStopFormula(runtime), Unit)
            .output { startListening() }
            .apply { formula.incrementEvents.triggerEvent() }
            .output { stopListening() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply {
                val expected = listOf(0, 0, 1, 1)
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test
    fun `each child event handler should be scoped to latest state`() {
        runtime.test(MultipleChildEvents.formula(), Unit)
            .output { child.incrementAndMessage() }
            .output { child.incrementAndMessage() }
            .output { child.incrementAndMessage() }
            .output { assertThat(state).isEqualTo(3) }
    }

    @Test
    fun `multiple child worker updates`() {
        ChildStreamEvents(runtime)
            .startListening()
            .incrementBy(3)
            .assertCurrentValue(3)
    }

    @Test
    fun `child worker is removed`() {
        ChildStreamEvents(runtime)
            .startListening()
            .incrementBy(2)
            .stopListening()
            .incrementBy(4)
            .assertCurrentValue(2)
    }

    @Test
    fun `parent removes child when child emits a message`() {
        ChildRemovedOnMessage(runtime)
            .assertChildIsVisible(true)
            .closeByChildMessage()
            .assertChildIsVisible(false)
    }

    @Test
    fun `transition after no re-evaluation pass`() {
        val sideEffectCallback = TestCallback()
        runtime.test(TransitionAfterNoEvaluationPass.formula(sideEffectCallback), Unit)
            .output { triggerSideEffect() }
            .output { triggerSideEffect() }
            .assertOutputCount(1)
            .apply {
                sideEffectCallback.assertTimesCalled(2)
            }
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        val sideEffectCallback = TestCallback()
        runtime.test(ChildTransitionAfterNoEvaluationPass.formula(sideEffectCallback), Unit)
            .output { child.triggerSideEffect() }
            .output { child.triggerSideEffect() }
            .assertOutputCount(1)
            .apply {
                sideEffectCallback.assertTimesCalled(2)
            }
    }

    @Test
    fun `nested child transition after no re-evaluation pass`() {

        val sideEffectCallback = TestCallback()
        runtime.test(NestedChildTransitionAfterNoEvaluationPass.formula(sideEffectCallback), Unit)
            .output { child.child.triggerSideEffect() }
            .output { child.child.triggerSideEffect() }
            .assertOutputCount(1)
            .apply {
                sideEffectCallback.assertTimesCalled(2)
            }
    }

    @Test
    fun `runtime emits messages`() {
        val messageHandler = TestEventCallback<Int>()
        runtime.test(MessageFormula(), MessageFormula.Input(messageHandler = messageHandler))
            .output { triggerMessage() }
            .assertOutputCount(1) // no state change, so no re-evaluation
            .apply {
                assertThat(messageHandler.values()).hasSize(1)
            }
    }

    @Test
    fun `message after no re-evaluation pass`() {
        val messageHandler = TestEventCallback<Int>()
        runtime.test(MessageFormula(), MessageFormula.Input(messageHandler = messageHandler))
            .output { triggerMessage() }
            .output { triggerMessage() }
            .assertOutputCount(1)
            .apply {
                assertThat(messageHandler.values()).hasSize(2)
            }
    }

    @Test
    fun `child message with no parent state change`() {
        runtime.test(ChildMessageNoParentStateChange.formula(), Unit)
            .output { child.triggerMessage() }
            .assertOutputCount(1)  // no state change, so no re-evaluation
    }

    @Test
    fun `child message with parent state change`() {
        runtime.test(ChildMessageWithParentStateChange.formula(), Unit)
            .output { child.triggerMessage() }
            .assertOutputCount(2)
            .output { assertThat(state).isEqualTo(1) }
    }

    @Test
    fun `side effect triggers parent state transition`() {
        runtime.test(ChildMessageTriggersEventTransitionInParent.formula(), Unit)
            .output { child.triggerSideEffect() }
            .output {
                assertThat(count).isEqualTo(1)
            }
    }

    @Test
    fun `child state is reset after toggle`() {
        runtime.test(ChildStateResetAfterToggle.formula(), Unit)
            .output { child!!.incrementAndMessage() }
            .output { child!!.incrementAndMessage() }
            .output { assertThat(child!!.state).isEqualTo(2) }
            .output { toggleChild() }
            .output { assertThat(child).isNull() }
            .output { toggleChild() }
            .output { assertThat(child!!.state).isEqualTo(0) }
    }

    @Test
    fun `multiple callbacks using the same render model`() {
        runtime.test(MessageFormula(), MessageFormula.Input(messageHandler = {}))
            .output {
                incrementAndMessage()
                incrementAndMessage()
                incrementAndMessage()
            }
            .output {
                assertThat(state).isEqualTo(3)
            }
    }

    @Test
    fun `multiple event callbacks using the same render model`() {
        runtime.test(EventCallbackFormula(), Unit)
            .output {
                changeState("one")
                changeState("two")
                changeState("three")
            }
            .output { assertThat(state).isEqualTo("three") }
    }

    @Test
    fun `using a removed child callback should do nothing`() {
        val formula = OptionalChildFormula(MessageFormula()) {
            MessageFormula.Input(messageHandler = onEvent<Int> { none() })
        }
        runtime.test(formula, Unit)
            .output {
                val cachedChild = child!!
                toggleChild()

                cachedChild.incrementAndMessage()
                cachedChild.incrementAndMessage()
                cachedChild.incrementAndMessage()
            }
            .output { assertThat(child).isNull() }
    }

    @Test
    fun `callbacks are equal across render model changes`() {
        runtime.test(MessageFormula(), MessageFormula.Input(messageHandler = {}))
            .output { incrementAndMessage() }
            .output { incrementAndMessage() }
            .assertOutputCount(3)
            .apply {
                assertThat(values().map { it.incrementAndMessage }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `event callbacks are equal across render model changes`() {
        runtime.test(EventCallbackFormula(), Unit)
            .output {
                changeState("one")
                changeState("two")
            }
            .assertOutputCount(3)
            .apply {
                assertThat(values().map { it.changeState }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `removed callback is disabled`() {
        runtime.test(OptionalCallbackFormula(), Unit)
            .output {
                callback?.invoke()
                toggleCallback()
                callback?.invoke()
            }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 1, 1).inOrder()
            }
    }

    @Test
    fun `callbacks are not the same after removing then adding it again`() {
        runtime.test(OptionalCallbackFormula(), Unit)
            .output {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.callback }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `removed event callback is disabled`() {
        runtime.test(OptionalEventCallbackFormula(), Unit)
            .output {
                callback?.invoke(1)
                toggleCallback()
                callback?.invoke(5)
            }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 1, 1).inOrder()
            }
    }

    @Test
    fun `event callbacks are not the same after removing then adding it again`() {
        runtime.test(OptionalEventCallbackFormula(), Unit)
            .output {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.callback }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `using callbacks within another function crashes`() {
        val result = Try { UsingCallbacksWithinAnotherFunction.test(runtime) }
        assertThat(result.errorOrNull()?.cause).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `using key to scope callbacks within another function`() {
        val formula = UsingKeyToScopeCallbacksWithinAnotherFunction.TestFormula()
        runtime.test(formula, Unit)
            .assertOutputCount(1)
    }

    @Test
    fun `remove item from a list using a key block for each item`() {
        KeyUsingListFormula
            .test(runtime, items = listOf("one", "two", "three"))
            .output {
                items[1].onDeleteSelected()
            }
            .output { assertThat(items).hasSize(2) }
    }

    @Test
    fun `subscribes to updates before delivering messages`() {
        SubscribesToAllUpdatesBeforeDeliveringMessages
            .test(runtime)
            .output {
                assertThat(this).isEqualTo(4)
            }
            .assertOutputCount(1)
    }

    @Test
    fun `init message executed once`() {
        StreamInitMessageDeliveredOnce.test(runtime).apply {
            assertThat(formula.timesInitializedCalled).isEqualTo(1)
        }
    }

    @Test
    fun `input changed message`() {
        runtime.test(StreamInputFormula())
            .input(0)
            .input(1)
            .input(2)
            .apply {
                assertThat(formula.messages).containsExactly(0, 1, 2).inOrder()
            }
    }

    @Test
    fun `events api ignores duplicate inputs`() {
        runtime.test(StreamInputFormula())
            .input(0)
            .input(0)
            .input(0)
            .input(0)
            .apply {
                assertThat(formula.messages).containsExactly(0).inOrder()
            }
    }

    @Test
    fun `remove all streams`() {
        DynamicStreamSubject(runtime)
            .updateStreams("one", "two", "three")
            .removeAll()
    }

    @Test
    fun `switch one stream`() {
        DynamicStreamSubject(runtime)
            .updateStreams("one", "two", "three")
            .updateStreams("one", "three", "four")
    }

    @Test
    fun `same stream declarations are okay`() {
        val formula = OnlyUpdateFormula<Unit> {
            events(EmptyStream.init()) {
                transition(Unit)
            }

            events(EmptyStream.init()) {
                transition(Unit)
            }
        }

        runtime.test(formula, Unit)
            .assertOutputCount(1)
    }

    @Test
    fun `same observable declarations are okay`() {
        val formula = OnlyUpdateFormula<Unit> {
            events(RxStream.fromObservable("same") { Observable.just(1) }) {
                none()
            }

            events(RxStream.fromObservable("same") { Observable.just(1) }) {
                none()
            }
        }

        runtime.test(formula, Unit).assertOutputCount(1)
    }

    @Test
    fun `key is required when stream is declared in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(1, 2, 3)
            list.forEach {
                events(EmptyStream.init()) {
                    none()
                }
            }
        }

        val error = Try { runtime.test(formula, Unit) }.errorOrNull()?.cause
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `using key for stream declared in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(1, 2, 3)
            list.forEach {
                events(EmptyStream.init(it)) {
                    none()
                }
            }
        }

        runtime.test(formula, Unit).assertOutputCount(1)
    }

    @Test
    fun `multiple event streams without key`() {
        var executed = 0
        val formula = OnlyUpdateFormula<Unit> {
            events(Stream.onInit()) {
                transition {
                    executed += 1
                }
            }

            events(Stream.onInit()) {
                transition {
                    executed += 1
                }
            }
        }

        runtime.test(formula, Unit).apply {
            assertThat(executed).isEqualTo(2)
        }
    }

    @Test
    fun `multiple events with input and without key`() {
        var executed = 0
        val formula = OnlyUpdateFormula<Int> {
            events(Stream.onData(it)) {
                transition { executed += 1 }
            }

            events(Stream.onData(it)) {
                transition { executed += 1 }
            }
        }

        runtime.test(formula, 1).apply {
            assertThat(executed).isEqualTo(2)
        }
    }

    @Test
    fun `key is required for events in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(0, 1, 2)
            list.forEach {
                events(Stream.onInit()) {
                    none()
                }
            }
        }

        val error = Try { runtime.test(formula, Unit) }.errorOrNull()?.cause
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `disposing formula triggers terminate message`() {
        runtime.test(
            TerminateFormula(),
            Unit
        )
            .apply {
                assertThat(formula.timesTerminateCalled).isEqualTo(0)
            }
            .dispose()
            .apply {
                assertThat(formula.timesTerminateCalled).isEqualTo(1)
            }
    }

    @Test
    fun `removing child formula triggers terminate message`() {
        val terminateFormula = TerminateFormula()
        val formula = OptionalChildFormula(terminateFormula)
        runtime.test(formula, Unit)
            .apply {
                assertThat(terminateFormula.timesTerminateCalled).isEqualTo(0)
            }
            .output { toggleChild() }
            .apply {
                assertThat(terminateFormula.timesTerminateCalled).isEqualTo(1)
            }
    }

    @Test
    fun `terminate message is scoped to latest input`() {
        var emissions = 0
        var terminateCallback = -1
        val formula = OnlyUpdateFormula<Int> { input ->
            events(Stream.onTerminate()) {
                transition {
                    emissions += 1
                    terminateCallback = input
                }
            }
        }

        runtime.test(formula)
            .input(1)
            .input(2)
            .input(3)
            .dispose()
            .apply {
                assertThat(emissions).isEqualTo(1)
                assertThat(terminateCallback).isEqualTo(3)
            }
    }

    // Stream tests

    @Test fun `stream formula emits initial value`() {
        runtime.test(runtime.streamFormula())
            .input("initial")
            .apply {
                Truth.assertThat(values()).containsExactly(0).inOrder()
            }
    }

    @Test fun `stream formula emits initial value and subsequent events`() {
        runtime.test(runtime.streamFormula())
            .input("initial")
            .apply {
                formula.emitEvent(1)
                formula.emitEvent(2)
                formula.emitEvent(3)
            }
            .apply {
                assertThat(values()).containsExactly(0, 1, 2, 3).inOrder()
            }
    }

    @Test fun `stream formula resets state when input changes`() {
        runtime.test(runtime.streamFormula())
            .input("initial")
            .apply { formula.emitEvent(1) }
            .input("reset")
            .apply { formula.emitEvent(1) }
            .apply {
                assertThat(values()).containsExactly(0, 1, 0, 1).inOrder()
            }
    }

    @Test fun `stream event callback is scoped to latest state`() {
        val events = listOf("a", "b")
        val formula = EventFormula(runtime, events)

        val expectedStates = listOf(1, 2)
        runtime.test(formula, Unit).apply {
            assertThat(formula.capturedStates()).isEqualTo(expectedStates)
        }
    }

    @Test fun `stream events are captured in order`() {
        val events = listOf("first", "second", "third", "third")
        val formula = EventFormula(runtime, events)
        runtime.test(formula, Unit).apply {
            assertThat(formula.capturedEvents()).isEqualTo(events)
        }
    }

    @Test fun `stream event callbacks can handle at least 100k events`() {
        val eventCount = 100000
        val events = (1..eventCount).toList()
        val formula = EventFormula(runtime, events)
        runtime.test(formula, Unit)
            .apply {
                assertThat(values()).containsExactly(eventCount).inOrder()
            }
    }

    // End of stream tests

    // Child specific test cases

    @Test fun `child formula input changes`() {
        val formula = DelegateFormula("default")
        runtime.test(formula, Unit)
            .output { onChildValueChanged("first") }
            .output { onChildValueChanged("second") }
            .apply {
                val expected = listOf("default", "first", "second")
                assertThat(values().map { it.childValue }).isEqualTo(expected)
            }
    }

    @Test fun `adding duplicate child throws an exception`() {
        val formula = object : StatelessFormula<Unit, List<Unit>>() {
            override fun evaluate(
                input: Unit,
                context: FormulaContext<Unit>
            ): Evaluation<List<Unit>> {
                return Evaluation(
                    output = listOf(1, 2, 3).map {
                        context.child(EmptyFormula())
                    }
                )
            }
        }

        val error = Try { runtime.test(formula, Unit) }.errorOrNull()?.cause
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `parent removal triggers childs terminate message`() {
        val terminateFormula = TerminateFormula()
        val formula = OptionalChildFormula(HasChildFormula(terminateFormula))

        runtime.test(formula, Unit).output { toggleChild() }.apply {
            assertThat(terminateFormula.timesTerminateCalled).isEqualTo(1)
        }
    }


    // End of child specific test cases

    @Test
    fun `multiple termination side-effects`() {
        val terminateFormula = TerminateFormula()
        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
                (0 until 10).forEach {
                    context.child(terminateFormula.id(it))
                }
                return Evaluation(Unit)
            }
        }
        runtime.test(formula, Unit).dispose()
        assertThat(terminateFormula.timesTerminateCalled).isEqualTo(10)
    }

    @Test fun `nested termination with input changed`() {
        runtime.test(NestedTerminationWithInputChanged())
            .input(false)
            .input(true)
            .input(false)
            .apply {
                assertThat(formula.terminateFormula.timesTerminateCalled).isEqualTo(1)
            }
    }

    @Test
    fun `canceling terminate stream does not emit terminate message`() {
        val terminateCallback = TestCallback()
        runtime.test(RemovingTerminateStreamSendsNoMessagesFormula())
            .input(RemovingTerminateStreamSendsNoMessagesFormula.Input(onTerminate = terminateCallback))
            .input(RemovingTerminateStreamSendsNoMessagesFormula.Input(onTerminate = null))
            .apply {
                terminateCallback.assertTimesCalled(0)
            }
    }

    @Test
    fun `using from observable with input`() {
        val onItem = TestEventCallback<FromObservableWithInputFormula.Item>()
        runtime.test(FromObservableWithInputFormula())
            .input(FromObservableWithInputFormula.Input("1", onItem = onItem))
            .input(FromObservableWithInputFormula.Input("2", onItem = onItem))
            .apply {
                assertThat(onItem.values()).containsExactly(
                    FromObservableWithInputFormula.Item("1"),
                    FromObservableWithInputFormula.Item("2")
                ).inOrder()
            }
    }

    @Test
    fun `emit error`() {
        val error = Try { EmitErrorTest.test(runtime) }.errorOrNull()?.cause
        assertThat(error?.message).isEqualTo("crashed")
    }

    @Test
    fun `initialize 100 levels nested formula`() {
        val formula = ExtremelyNestedFormula.nested(100)
        runtime.test(formula, Unit).output {
            assertThat(this).isEqualTo(100)
        }
    }

    @Test
    fun `initialize 250 levels nested formula`() {
        val formula = ExtremelyNestedFormula.nested(250)
        runtime.test(formula, Unit).output {
            assertThat(this).isEqualTo(250)
        }
    }

    @Ignore("stack overflows when there are 500 nested child formulas")
    @Test
    fun `initialize 500 levels nested formula`() {
        val formula = ExtremelyNestedFormula.nested(500)
        runtime.test(formula, Unit).output {
            assertThat(this).isEqualTo(500)
        }
    }

    @Test
    fun `mixing callback use with key use`() {
        val formula = MixingCallbackUseWithKeyUse.ParentFormula()
        runtime.test(formula, Unit).assertOutputCount(1)
    }

    // TODO: maybe worth adding support eventually.
    @Test
    fun `nested keys are not allowed`() {
        val error = Try { runtime.test(NestedKeyFormula(), Unit) }.errorOrNull()?.cause
        assertThat(error)
            .apply { isInstanceOf(IllegalStateException::class.java)  }
            .hasMessageThat().startsWith("Nested scopes are not supported currently.")
    }

    @Test
    fun `formula key is used to reset root formula state`() {
        RootFormulaKeyTestSubject(runtime)
            .assertValue(0)
            .increment()
            .increment()
            .assertValue(2)
            .resetKey()
            .assertValue(0)
    }
}
