package com.instacart.formula

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.actions.EmptyAction
import com.instacart.formula.internal.ClearPluginsRule
import com.instacart.formula.internal.FormulaKey
import com.instacart.formula.internal.TestInspector
import com.instacart.formula.internal.Try
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.subjects.ChildActionFiresParentEventOnStart
import com.instacart.formula.subjects.ChildMessageNoParentStateChange
import com.instacart.formula.subjects.ChildMessageTriggersEventTransitionInParent
import com.instacart.formula.subjects.ChildMessageWithParentStateChange
import com.instacart.formula.subjects.ChildRemovedOnMessage
import com.instacart.formula.subjects.ChildStateResetAfterToggle
import com.instacart.formula.subjects.ChildStreamEvents
import com.instacart.formula.subjects.ChildTransitionAfterNoEvaluationPass
import com.instacart.formula.subjects.CombinedParentAndChildStateChange
import com.instacart.formula.subjects.CombinedParentAndChildStateChangeOnEvent
import com.instacart.formula.subjects.DelegateFormula
import com.instacart.formula.subjects.DuplicateListenerKeysHandledByIndexing
import com.instacart.formula.subjects.DynamicParentFormula
import com.instacart.formula.subjects.DynamicStreamSubject
import com.instacart.formula.subjects.EffectOrderFormula
import com.instacart.formula.subjects.EventCallbackFormula
import com.instacart.formula.subjects.EventFormula
import com.instacart.formula.subjects.ExtremelyNestedFormula
import com.instacart.formula.subjects.FromObservableWithInputFormula
import com.instacart.formula.subjects.HasChildFormula
import com.instacart.formula.subjects.IncrementingDispatcher
import com.instacart.formula.subjects.InputChangeWhileFormulaRunningRobot
import com.instacart.formula.subjects.KeyFormula
import com.instacart.formula.subjects.KeyUsingListFormula
import com.instacart.formula.subjects.MessageFormula
import com.instacart.formula.subjects.MixingCallbackUseWithKeyUse
import com.instacart.formula.subjects.MultiChildIndirectStateChangeRobot
import com.instacart.formula.subjects.MultiThreadRobot
import com.instacart.formula.subjects.MultipleChildEvents
import com.instacart.formula.subjects.NestedCallbackCallRobot
import com.instacart.formula.subjects.NestedChildTransitionAfterNoEvaluationPass
import com.instacart.formula.subjects.NestedKeyFormula
import com.instacart.formula.subjects.NestedTerminationWithInputChanged
import com.instacart.formula.subjects.NullableStateFormula
import com.instacart.formula.subjects.OnlyUpdateFormula
import com.instacart.formula.subjects.OptionalCallbackFormula
import com.instacart.formula.subjects.OptionalChildFormula
import com.instacart.formula.subjects.OptionalEventCallbackFormula
import com.instacart.formula.subjects.ParallelChildFormulaFiresEventOnStart
import com.instacart.formula.subjects.ParentTransitionOnChildActionStart
import com.instacart.formula.subjects.ParentUpdateChildAndSelfOnEventRobot
import com.instacart.formula.subjects.PendingActionFormulaTerminatedOnActionInit
import com.instacart.formula.subjects.RemovingTerminateStreamSendsNoMessagesFormula
import com.instacart.formula.subjects.ReusableFunctionCreatesUniqueListeners
import com.instacart.formula.subjects.RootFormulaKeyTestSubject
import com.instacart.formula.subjects.RunAgainActionFormula
import com.instacart.formula.subjects.SleepFormula
import com.instacart.formula.subjects.StartStopFormula
import com.instacart.formula.subjects.StateTransitionTimingFormula
import com.instacart.formula.subjects.StreamInitMessageDeliveredOnce
import com.instacart.formula.subjects.StreamInputFormula
import com.instacart.formula.subjects.SubscribesToAllUpdatesBeforeDeliveringMessages
import com.instacart.formula.subjects.TerminateFormula
import com.instacart.formula.subjects.TestDispatcherPlugin
import com.instacart.formula.subjects.TestKey
import com.instacart.formula.subjects.TransitionAfterNoEvaluationPass
import com.instacart.formula.subjects.UniqueListenersWithinLoop
import com.instacart.formula.subjects.UseInputFormula
import com.instacart.formula.subjects.UsingKeyToScopeCallbacksWithinAnotherFunction
import com.instacart.formula.subjects.UsingKeyToScopeChildFormula
import com.instacart.formula.test.CoroutinesTestableRuntime
import com.instacart.formula.test.CountingInspector
import com.instacart.formula.test.RxJavaTestableRuntime
import com.instacart.formula.test.TestCallback
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.TestableRuntime
import com.instacart.formula.types.ActionDelegateFormula
import com.instacart.formula.types.IncrementFormula
import com.instacart.formula.types.OnEventFormula
import com.instacart.formula.types.OnInitActionFormula
import io.reactivex.rxjava3.core.Observable
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

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
    val rule = RuleChain
        .outerRule(TestName())
        .around(ClearPluginsRule())
        .around(runtime.rule)

    @Test fun `state change triggers an evaluation`() {
        val formula = EventCallbackFormula()
        runtime.test(formula, Unit)
            .output { changeState("state 1") }
            .output { changeState("state 2") }
            .apply {
                val expected = listOf("", "state 1", "state 2")
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test fun `state change is ignored if value is the same as last value`() {
        val formula = EventCallbackFormula()
        runtime.test(formula, Unit)
            .output { changeState("state 1") }
            .output { changeState("state 1") }
            .apply {
                val expected = listOf("", "state 1")
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test fun `state can be a null value`() {
        val formula = NullableStateFormula()
        runtime.test(formula, Unit)
            .output { assertThat(state).isNull() }
            .output { updateState("new state") }
            .output { assertThat(state).isEqualTo("new state") }
            .output { updateState(null) }
            .output { assertThat(state).isNull() }
    }

    @Test
    fun `state change is performed before transition side-effects`() {
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

    @Test fun `input change invokes onInputChanged`() {
        val formula = UseInputFormula<String>()
        runtime.test(formula)
            .input("first")
            .input("second")
            .apply {
                assertThat(values()).containsExactly("first", "second").inOrder()
            }
    }

    @Test fun `input change triggers an evaluation`() {
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(output = input)
            }
        }

        runtime.test(formula)
            .input(1)
            .input(2)
            .input(3)
            .apply {
                assertThat(values()).containsExactly(1, 2, 3).inOrder()
            }
    }

    @Test fun `input change is ignored if value is the same as last value`() {
        val formula = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(output = input)
            }
        }

        runtime.test(formula)
            .input(1)
            .input(1)
            .apply {
                val expected = listOf(1)
                assertThat(values()).isEqualTo(expected)
            }
    }

    @Test fun `input change happens while formula is running`() {
        val robot = InputChangeWhileFormulaRunningRobot(runtime, eventCount = 3)

        // Start formula
        robot.test.input(0)

        // Input
        robot.test.output { assertThat(this).isEqualTo(3) }
    }

    @Test
    fun `multiple event updates`() {
        runtime.test(StartStopFormula(runtime), Unit)
            .output { startListening() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply { formula.incrementEvents.triggerEvent() }
            .apply {
                val expected = listOf(0, 1, 2, 3)
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
                val expected = listOf(0, 1)
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
        val inspector = CountingInspector()
        runtime.test(ChildMessageWithParentStateChange.formula(), Unit, inspector)
            .output { child.triggerMessage() }
            .assertOutputCount(2)
            .output { assertThat(state).isEqualTo(1) }

        inspector.assertRunCount(2)
        inspector.assertEvaluationCount(3)
    }

    @Test
    fun `immediate child transition triggers parent state change`() {
        val formula = ParentTransitionOnChildActionStart.formula(eventNumber = 3)
        val inspector = CountingInspector()
        runtime.test(formula, Unit, inspector)
            .output { assertThat(state).isEqualTo(3) }

        // TODO: run count could be reduced to 1 with inline effect execution
        inspector.assertRunCount(4)
        inspector.assertEvaluationCount(HasChildFormula::class, 4)
        inspector.assertEvaluationCount(OnInitActionFormula::class, 1)
    }

    @Test
    fun `immediate child transition triggers parent state change in nested situation`() {
        val parentTransitionFormula = ParentTransitionOnChildActionStart.formula(eventNumber = 3)
        // Nest it within HasChildFormula
        val formula = HasChildFormula(parentTransitionFormula)
        val inspector = CountingInspector()
        runtime.test(formula, Unit, inspector)
            .output { assertThat(child.state).isEqualTo(3) }


        // TODO: run count could be reduced to 1 with inline effect execution
        inspector.assertRunCount(4)
        inspector.assertEvaluationCount(HasChildFormula::class, 8)
        inspector.assertEvaluationCount(OnInitActionFormula::class, 1)
    }

    @Test
    fun `child action triggers parent event on start`() {
        val increments = listOf(1, 1, 1, 1, 1, 1)
        runtime.test(
            formula = ChildActionFiresParentEventOnStart.formula(runChildOnStart = true, increments),
            input = Unit,
        )
            .output { assertThat(value).isEqualTo(6) }
            .apply {
                // Efficiently we emit only emit 2 values
                assertThat(values()).hasSize(1)
            }

        runtime.test(
            formula = ChildActionFiresParentEventOnStart.formula(runChildOnStart = false, increments),
            input = Unit,
        )
            .output { assertThat(value).isEqualTo(0) }
            .output { showChild(true) }
            .output { assertThat(value).isEqualTo(6) }
            .apply {
                // Efficiently we emit only emit 2 values
                assertThat(values()).hasSize(2)
            }
    }

    @Test
    fun `on action start child triggers state change in a parallel child`() {
        val eventNumber = 4
        val formula = ParallelChildFormulaFiresEventOnStart.formula(eventNumber)
        val inspector = CountingInspector()
        runtime.test(formula, Unit, inspector)
            .apply {
                inspector.assertActionsStarted(1)
                inspector.assertStateTransitions(IncrementFormula::class, 4)
            }
            .output { assertThat(this).isEqualTo(4) }
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
    fun `child start action triggers self and parent state changes`() {
        val inspector = CountingInspector()
        val formula = CombinedParentAndChildStateChange.formula()
        runtime.test(formula, Unit, inspector)
            .output {
                assertThat(state).isEqualTo(1)
                assertThat(child).isEqualTo(1)
            }

        inspector.assertRunCount(2)
        inspector.assertEvaluationCount(ActionDelegateFormula::class, 2)
        inspector.assertEvaluationCount(HasChildFormula::class, 2)
    }

    @Test
    fun `child event triggers self and parent state changes`() {
        val inspector = CountingInspector()
        val formula = CombinedParentAndChildStateChangeOnEvent.formula()
        runtime.test(formula, Unit, inspector)
            .output { child.onEvent() }
            .output {
                assertThat(state).isEqualTo(1)
                assertThat(child.state).isEqualTo(1)
            }

        inspector.assertRunCount(3)
        inspector.assertEvaluationCount(OnEventFormula::class, 2)
        inspector.assertEvaluationCount(HasChildFormula::class, 3)
    }

    @Test fun `transition effect queue maintains FIFO order when starting a new stream during a transition`() {
        val inspector = CountingInspector()
        val onEvent = TestEventCallback<EffectOrderFormula.Event>()
        val initialInput = EffectOrderFormula.Input(onEvent = onEvent)
        runtime.test(EffectOrderFormula(), initialInput, inspector)
            .output { triggerEvent() }
            .output { triggerEvent() }

        val expected = listOf(
            EffectOrderFormula.Event(1),
            EffectOrderFormula.Event(2),
            EffectOrderFormula.Event(3),
            EffectOrderFormula.Event(4)
        )
        assertThat(onEvent.values()).isEqualTo(expected)

        inspector.assertRunCount(3)
        inspector.assertEvaluationCount(5)
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
    fun `multiple listeners using the same render model`() {
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
    fun `multiple event listeners using the same render model`() {
        runtime.test(EventCallbackFormula(), Unit)
            .output {
                changeState("one")
                changeState("two")
                changeState("three")
            }
            .output { assertThat(state).isEqualTo("three") }
    }

    @Test
    fun `using a removed child listener should do nothing`() {
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
    fun `listeners are equal across render model changes`() {
        runtime.test(MessageFormula(), MessageFormula.Input(messageHandler = {}))
            .output { incrementAndMessage() }
            .output { incrementAndMessage() }
            .assertOutputCount(3)
            .apply {
                assertThat(values().map { it.incrementAndMessage }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `event listeners are equal across render model changes`() {
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
    fun `removed listener is disabled`() {
        runtime.test(OptionalCallbackFormula(), Unit)
            .output {
                listener?.invoke()
                toggleCallback()
                listener?.invoke()
            }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 1, 1).inOrder()
            }
    }

    @Test
    fun `listeners are not the same after removing then adding it again`() {
        runtime.test(OptionalCallbackFormula(), Unit)
            .output {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.listener }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `removed event listener is disabled`() {
        runtime.test(OptionalEventCallbackFormula(), Unit)
            .output {
                listener?.invoke(1)
                toggleListener()
                listener?.invoke(5)
            }
            .apply {
                assertThat(values().map { it.state }).containsExactly(0, 1, 1).inOrder()
            }
    }

    @Test
    fun `event listeners are not the same after removing then adding it again`() {
        runtime.test(OptionalEventCallbackFormula(), Unit)
            .output {
                toggleListener()
                toggleListener()
            }
            .apply {
                assertThat(values().map { it.listener }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `reusable function returns unique listeners`() {
        val subject = ReusableFunctionCreatesUniqueListeners.test(runtime)
        subject.output { assertThat(firstListener).isNotEqualTo(secondListener) }
    }

    @Test
    fun `creating listener within a loop returns a unique listener`() {
        val subject = UniqueListenersWithinLoop.test(runtime)
        subject.output { assertThat(listeners).containsNoDuplicates() }
    }

    @Test
    fun `duplicate listener keys are handled by indexing`() {
        val subject = DuplicateListenerKeysHandledByIndexing.test(runtime)
        subject.output { assertThat(listeners).containsNoDuplicates() }
    }

    @Test
    fun `using key to scope listeners within another function`() {
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

    // Stream test cases

    @Test fun `stream event triggers a state change`() {
        val formula = StartStopFormula(runtime)
        runtime.test(formula, Unit)
            .output { this.startListening() }
            .output { assertThat(state).isEqualTo(0) }
            .apply { formula.incrementEvents.triggerEvent() }
            .output { assertThat(state).isEqualTo(1) }
    }

    @Test fun `stream triggers only a side-effect`() {
        val eventCallback = TestEventCallback<String>()
        val events = listOf("a", "b")
        val formula = OnlyUpdateFormula<Unit> {
            runtime.emitEvents(events).onEvent {
                transition {
                    eventCallback(it)
                }
            }
        }

        val observer = runtime.test(formula, Unit)
        assertThat(observer.values()).containsExactly(Unit).inOrder()
        assertThat(eventCallback.values()).containsExactly("a", "b").inOrder()
    }

    @Test fun `stream is disposed when evaluation does not contain it`() {
        DynamicStreamSubject(runtime)
            .updateStreams(keys = arrayOf("1"))
            .assertRunning(keys = arrayOf("1"))
            .updateStreams(keys = emptyArray())
            .assertRunning(keys = emptyArray())
    }

    @Test fun `stream is removed when formula is removed`() {
        DynamicStreamSubject(runtime)
            .updateStreams(keys = arrayOf("1"))
            .assertRunning(keys = arrayOf("1"))
            .dispose()
            .assertRunning(keys = emptyArray())
    }

    @Test fun `stream is reset when key changes`() {
        DynamicStreamSubject(runtime)
            .updateStreams("1")
            .assertRunning("1")
            .updateStreams("2")
            .assertRunning("2")
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

    @Test fun `parent updates a child and self in a single action`() {
        val robot = ParentUpdateChildAndSelfOnEventRobot(runtime)
        robot.start()
        robot.subject.output { onAction() }
        robot.subject.output {
            assertThat(childValue).isEqualTo(1)
            assertThat(parentValue).isEqualTo(3)
        }
    }

    @Test
    fun `formula calls an event listener from a transition`() {
        val robot = NestedCallbackCallRobot(runtime)
        robot.start()
        robot.subject.output { onAction() }
        robot.subject.output { assertThat(value).isEqualTo(1) }
    }

    @Test
    fun `formula calls own event listener which starts multiple transitions`() {
        val robot = MultiChildIndirectStateChangeRobot(runtime)
        robot.start()
        robot.subject.output { onAction() }
        robot.subject.output {
            assertThat(childValue).isEqualTo(2)
            assertThat(parentValue).isEqualTo(3)
        }
    }

    @Test
    fun `action runAgain`() {
        val inspector = CountingInspector()
        runtime
            .test(RunAgainActionFormula(), Unit, inspector)
            .output {
                assertThat(actionExecuted).isEqualTo(1)
                assertThat(nullableActionExecuted).isEqualTo(0)
                assertThat(customActionExecuted).isEqualTo(0)
            }
            .output {
                runActionAgain.invoke()
                runNullableActionAgain.invoke()
                runCustomAction.invoke()
            }
            .output {
                assertThat(actionExecuted).isEqualTo(2)
                assertThat(nullableActionExecuted).isEqualTo(1)
                assertThat(customActionExecuted).isEqualTo(1)
            }
            .output {
                runActionAgain.invoke()
                runNullableActionAgain.invoke()
                runCustomAction.invoke()
            }
            .output {
                assertThat(actionExecuted).isEqualTo(3)
                assertThat(nullableActionExecuted).isEqualTo(2)
                assertThat(customActionExecuted).isEqualTo(2)
            }

        inspector.assertEvaluationCount(14)
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
            .assertRunning(keys = emptyArray())
    }

    @Test
    fun `switch one stream`() {
        DynamicStreamSubject(runtime)
            .updateStreams("one", "two", "three")
            .updateStreams("one", "three", "four")
    }

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

    @Test fun `stream event listener is scoped to latest state`() {
        val events = listOf("a", "b")
        val formula = EventFormula(runtime, events)

        val inspector = CountingInspector()
        val expectedStates = listOf(1, 2)
        runtime.test(formula, Unit, inspector).apply {
            assertThat(formula.capturedStates()).isEqualTo(expectedStates)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(3)
    }

    @Test fun `stream events are captured in order`() {
        val inspector = CountingInspector()
        val events = listOf("first", "second", "third", "third")
        val formula = EventFormula(runtime, events)
        runtime.test(formula, Unit, inspector).apply {
            assertThat(formula.capturedEvents()).isEqualTo(events)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(5)
    }

    @Test fun `stream event listeners can handle at least 100k events`() {
        val inspector = CountingInspector()
        val eventCount = 100000
        val events = (1..eventCount).toList()
        val formula = EventFormula(runtime, events)
        runtime.test(formula, Unit, inspector)
            .apply {
                assertThat(values()).containsExactly(eventCount).inOrder()
            }

        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(100001)
    }

    @Test fun `child formula within multiple events on start`() {
        val inspector = CountingInspector()
        val eventCount = 100000
        val events = (1..eventCount).toList()
        val eventsFormula = EventFormula(runtime, events)
        val parent = HasChildFormula(eventsFormula)

        runtime.test(parent, Unit, inspector)
            .output { assertThat(child).isEqualTo(100000) }

        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(EventFormula::class, 100001)
        inspector.assertEvaluationCount(HasChildFormula::class, 1)
    }

    @Test
    fun `same stream declarations are okay`() {
        val formula = OnlyUpdateFormula<Unit> {
            EmptyAction.init().onEvent {
                transition(Unit)
            }

            EmptyAction.init().onEvent {
                transition(Unit)
            }
        }

        runtime.test(formula, Unit)
            .assertOutputCount(1)
    }

    @Test
    fun `same observable declarations are okay`() {
        val formula = OnlyUpdateFormula<Unit> {
            RxAction.fromObservable("same") { Observable.just(1) }.onEvent {
                none()
            }

            RxAction.fromObservable("same") { Observable.just(1) }.onEvent {
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
                EmptyAction.init().onEvent {
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
                EmptyAction.init(it).onEvent {
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
            Action.onInit().onEvent {
                transition {
                    executed += 1
                }
            }

            Action.onInit().onEvent {
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
            Action.onData(it).onEvent {
                transition { executed += 1 }
            }

            Action.onData(it).onEvent {
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
                Action.onInit().onEvent {
                    none()
                }
            }
        }

        val error = Try { runtime.test(formula, Unit) }.errorOrNull()?.cause
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `terminate formula with multiple pending actions on first action init`() {
        val robot = PendingActionFormulaTerminatedOnActionInit(runtime)
        // Starts the formula
        robot.test.input(Unit)

        // Single output should be emitted
        robot.assertActionsStarted(1)

        // No output is emitted because we unsubscribe before doing so
        robot.test.assertOutputCount(0)
    }

    @Test
    fun `disposing formula triggers terminate message`() {
        runtime.test(TerminateFormula(), Unit)
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
            Action.onTerminate().onEvent {
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

    // End of stream test cases

    // Child specific test cases

    @Test fun `child state change triggers parent formula evaluation`() {
        val childFormula = EventCallbackFormula()
        val formula = HasChildFormula(childFormula)
        runtime.test(formula, Unit)
            .output { child.changeState("new state") }
            .output { assertThat(child.state).isEqualTo("new state") }
    }

    @Test fun `child formula input change triggers an evaluation`() {
        val formula = DelegateFormula("default")
        runtime.test(formula, Unit)
            .output { changeChildInput("first") }
            .output { changeChildInput("second") }
            .apply {
                val expected = listOf("default", "first", "second")
                assertThat(values().map { it.childValue }).isEqualTo(expected)
            }
    }

    @Test
    fun `parent removes child when child emits a message`() {
        ChildRemovedOnMessage(runtime)
            .assertChildIsVisible(true)
            .closeByChildMessage()
            .assertChildIsVisible(false)
    }

    @Test fun `parent removes all child formulas`() {
        val formula = DynamicParentFormula()
        runtime.test(formula, Unit)
            .output { addChild(TestKey("1")) }
            .output { addChild(TestKey("2")) }
            .output { addChild(TestKey("3")) }
            .output {
                val expected = listOf("1", "2", "3")
                assertThat(children.map { it.input.id }).isEqualTo(expected)
            }
            .output { removeAllChildren() }
            .output {
                assertThat(children).isEmpty()
            }
    }

    @Test fun `adding duplicate child logs global event`() {
        val duplicateKeys = mutableListOf<Any>()

        FormulaPlugins.setPlugin(object : Plugin {
            override fun onDuplicateChildKey(
                parentType: Class<*>,
                childFormulaType: Class<*>,
                key: Any
            ) {
                duplicateKeys.add(key)
            }
        })

        val result = Try {
            val formula = DynamicParentFormula()
            runtime.test(formula, Unit)
                .output { addChild(TestKey("1")) }
                .output { addChild(TestKey("1")) }
        }

        // No errors
        val error = result.errorOrNull()?.cause
        assertThat(error).isNull()

        // Should log only once
        assertThat(duplicateKeys).hasSize(1)
        assertThat(duplicateKeys).containsExactly(
            FormulaKey(null, KeyFormula::class.java, TestKey("1"))
        )
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
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
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
        val formula = OnlyUpdateFormula<Unit> {
            Action.onInit().onEvent {
                throw java.lang.IllegalStateException("crashed")
            }
        }
        val error = Try { runtime.test(formula, Unit) }.errorOrNull()?.cause
        assertThat(error?.message).isEqualTo("crashed")
    }

    @Test
    fun `initialize 100 levels nested formula`() {
        val inspector = CountingInspector()
        val formula = ExtremelyNestedFormula.nested(100)
        runtime.test(formula, Unit, inspector).output {
            assertThat(this).isEqualTo(100)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(200)
    }

    @Test
    fun `initialize 250 levels nested formula`() {
        val inspector = CountingInspector()
        val formula = ExtremelyNestedFormula.nested(250)
        runtime.test(formula, Unit, inspector).output {
            assertThat(this).isEqualTo(250)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(500)
    }

    @Ignore("stack overflows when there are 500 nested child formulas")
    @Test
    fun `initialize 500 levels nested formula`() {
        val inspector = CountingInspector()
        val formula = ExtremelyNestedFormula.nested(500)
        runtime.test(formula, Unit, inspector).output {
            assertThat(this).isEqualTo(500)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(1000)
    }

    @Test
    fun `mixing listener use with key use`() {
        val formula = MixingCallbackUseWithKeyUse.ParentFormula()
        runtime.test(formula, Unit).assertOutputCount(1)
    }

    @Test
    fun `nested keys are allowed`() {
        val subject = runtime.test(NestedKeyFormula(), Unit)
        subject.assertNoErrors()
    }

    @Test
    fun `use key to scope child formula`() {
        val subject = runtime.test(UsingKeyToScopeChildFormula(), Unit)
        subject.output {
            assertThat(children).containsExactly(
                "value 1", "value 2"
            ).inOrder()
        }
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

    @Test
    fun `formula multi-thread handoff to executing thread`() {
        with(MultiThreadRobot(runtime)) {
            thread("thread-a", 50)
            thread("thread-b", 10)
            awaitCompletion()
            thread("thread-b", 10)

            awaitEvents(
                SleepFormula.SleepEvent(50, "thread-a"),
                // First thread-b event is handed-off to thread-a
                SleepFormula.SleepEvent(10, "thread-a"),
                // Second thread-b event is handled by thread-b
                SleepFormula.SleepEvent(10, "thread-b")
            )
        }
    }

    @Test
    fun `formula multi-threaded events fired at the same time`() {
        with(MultiThreadRobot(runtime)) {
            thread("a", 25)
            thread("b", 25)
            thread("c", 25)
            thread("d", 25)

            awaitEvents { events ->
                assertThat(events).hasSize(4)

                val durations = events.map { it.duration }
                assertThat(durations).containsExactly(25L, 25L, 25L, 25L)
            }
        }
    }

    @Test
    fun `formula multi-threaded input after termination`() {
        with(MultiThreadRobot(runtime)) {
            thread("a", 25)
            awaitCompletion()

            thread("c") { dispose() }
            thread("d") {
                // We delay to ensure that dispose is called first
                Thread.sleep(50)
                input("key-2")
            }

            awaitEvents { events ->
                assertThat(events).hasSize(1)
            }
        }
    }

    @Test
    fun `inspector events`() {
        val globalInspector = TestInspector()
        FormulaPlugins.setPlugin(object : Plugin {
            override fun inspector(type: KClass<*>): Inspector {
                return globalInspector
            }
        })

        val formula = StartStopFormula(runtime)
        val localInspector = TestInspector()
        val subject = runtime.test(formula, Unit, localInspector)
        subject.output { startListening() }
        subject.output { stopListening() }
        subject.dispose()

        for (inspector in listOf(globalInspector, localInspector)) {
            assertThat(inspector.events).containsExactly(
                "formula-run-started",
                "formula-started: com.instacart.formula.subjects.StartStopFormula",
                "evaluate-started: com.instacart.formula.subjects.StartStopFormula",
                "evaluate-finished: com.instacart.formula.subjects.StartStopFormula",
                "formula-run-finished",
                "state-changed: com.instacart.formula.subjects.StartStopFormula",
                "formula-run-started",
                "evaluate-started: com.instacart.formula.subjects.StartStopFormula",
                "evaluate-finished: com.instacart.formula.subjects.StartStopFormula",
                "action-started: com.instacart.formula.subjects.StartStopFormula",
                "formula-run-finished",
                "state-changed: com.instacart.formula.subjects.StartStopFormula",
                "formula-run-started",
                "evaluate-started: com.instacart.formula.subjects.StartStopFormula",
                "evaluate-finished: com.instacart.formula.subjects.StartStopFormula",
                "action-finished: com.instacart.formula.subjects.StartStopFormula",
                "formula-run-finished",
                "formula-finished: com.instacart.formula.subjects.StartStopFormula"
            ).inOrder()
        }
    }

    @Test
    fun `only global inspector events`() {
        val globalInspector = TestInspector()
        FormulaPlugins.setPlugin(object : Plugin {
            override fun inspector(type: KClass<*>): Inspector {
                return globalInspector
            }
        })

        val formula = StartStopFormula(runtime)
        val subject = runtime.test(formula, Unit)
        subject.dispose()

        assertThat(globalInspector.events).isNotEmpty()
    }

    @Test fun `only main dispatcher effect`() {
        val plugin = TestDispatcherPlugin()
        FormulaPlugins.setPlugin(plugin)

        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        Action.onInit().onEvent {
                            transition(Effect.Main) {
                                // main-effect
                            }
                        }
                    }
                )
            }
        }

        val subject = runtime.test(formula, Unit)
        plugin.mainDispatcher.assertCalled(1)
        plugin.backgroundDispatcher.assertCalled(0)
    }

    @Test fun `only background dispatcher effect`() {
        val plugin = TestDispatcherPlugin()
        FormulaPlugins.setPlugin(plugin)

        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        Action.onInit().onEvent {
                            transition(Effect.Background) {
                                // main-effect
                            }
                        }
                    }
                )
            }
        }

        val subject = runtime.test(formula, Unit)
        plugin.mainDispatcher.assertCalled(0)
        plugin.backgroundDispatcher.assertCalled(1)
    }

    @Test fun `combined dispatcher transition`() {
        val plugin = TestDispatcherPlugin()
        FormulaPlugins.setPlugin(plugin)

        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        Action.onInit().onEvent {
                            // Combined effect
                            val mainTransition = transition(Effect.Main) {

                            }
                            mainTransition.andThen {
                                transition(Effect.Background) {

                                }
                            }
                        }
                    }
                )
            }
        }

        val subject = runtime.test(formula, Unit)
        plugin.mainDispatcher.assertCalled(1)
        plugin.backgroundDispatcher.assertCalled(1)
    }
}

