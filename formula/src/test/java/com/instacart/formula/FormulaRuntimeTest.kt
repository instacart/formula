package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.actions.EmptyAction
import com.instacart.formula.batch.StateBatchScheduler
import com.instacart.formula.internal.ClearPluginsRule
import com.instacart.formula.internal.FormulaKey
import com.instacart.formula.internal.TestDispatcher
import com.instacart.formula.internal.TestInspector
import com.instacart.formula.internal.TestPlugin
import com.instacart.formula.internal.Try
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.plugin.Plugin
import com.instacart.formula.plugin.withPlugin
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.subjects.ChildActionFiresParentEventOnStart
import com.instacart.formula.subjects.ChildErrorAfterToggleFormula
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
import com.instacart.formula.subjects.HasChildrenFormula
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
import com.instacart.formula.test.CountingInspector
import com.instacart.formula.test.FlowRelay
import com.instacart.formula.test.TestCallback
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import com.instacart.formula.types.ActionDelegateFormula
import com.instacart.formula.types.IncrementActionFormula
import com.instacart.formula.types.IncrementFormula
import com.instacart.formula.types.InputIdentityFormula
import com.instacart.formula.types.OnEventFormula
import com.instacart.formula.types.OnInitActionFormula
import com.instacart.formula.types.TestStateBatchScheduler
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class FormulaRuntimeTest {

    @get:Rule
    val rule = RuleChain
        .outerRule(TestName())
        .around(ClearPluginsRule())

    @Test fun `state change triggers an evaluation`() {
        val formula = EventCallbackFormula()
        formula.test().input(Unit)
            .output { changeState("state 1") }
            .output { changeState("state 2") }
            .apply {
                val expected = listOf("", "state 1", "state 2")
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test fun `state change is ignored if value is the same as last value`() {
        val formula = EventCallbackFormula()
        formula.test().input(Unit)
            .output { changeState("state 1") }
            .output { changeState("state 1") }
            .apply {
                val expected = listOf("", "state 1")
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test fun `state can be a null value`() {
        val formula = NullableStateFormula()
        formula.test().input(Unit)
            .output { assertThat(state).isNull() }
            .output { updateState("new state") }
            .output { assertThat(state).isEqualTo("new state") }
            .output { updateState(null) }
            .output { assertThat(state).isNull() }
    }

    @Test
    fun `state change is performed before transition side-effects`() {
        // TODO: not sure if this test is very clear.
        val formula = StateTransitionTimingFormula()
        val expectedStates = listOf(
            StateTransitionTimingFormula.State.INTERNAL,
            StateTransitionTimingFormula.State.EXTERNAL
        )

        formula.test().input(Unit).output { onStateTransition() }.output {
            assertThat(events).isEqualTo(expectedStates)
        }
    }

    @Test fun `input change invokes onInputChanged`() {
        val formula = UseInputFormula<String>()
        formula.test()
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

        formula.test()
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

        formula.test()
            .input(1)
            .input(1)
            .apply {
                val expected = listOf(1)
                assertThat(values()).isEqualTo(expected)
            }
    }

    @Test fun `input change happens while formula is running`() {
        val robot = InputChangeWhileFormulaRunningRobot(eventCount = 3)

        // Start formula
        robot.test.input(0)

        // Input
        robot.test.output { assertThat(this).isEqualTo(3) }
    }

    @Test fun `input change while running triggers root formula restart`() {
        val terminationCallback = TestEventCallback<Int>()
        var observer: TestFormulaObserver<Int, *, *>? = null
        val root = object : StatelessFormula<Int, Int>() {
            override fun key(input: Int): Any {
                return input
            }

            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = input,
                    actions = context.actions {
                        Action.onTerminate().onEvent {
                            transition {
                                terminationCallback.invoke(input)
                            }
                        }

                        if (input == 0) {
                            Action.onInit().onEvent {
                                /**
                                 * We call observer explicitly outside of effect block to ensure
                                 * that input change happens while formula is running
                                 */
                                observer?.input(1)
                                none()
                            }
                        }
                    }
                )
            }
        }

        observer = root.test()
        observer.input(0)
        observer.output { assertThat(this).isEqualTo(1) }

        // Check that termination was called
        assertThat(terminationCallback.values()).containsExactly(0).inOrder()
    }

    @Test fun `runtime termination triggered while formula is running`() {
        val terminationCallback = TestEventCallback<Int>()
        var observer: TestFormulaObserver<Int, *, *>? = null
        val root = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = input,
                    actions = context.actions {
                        Action.onTerminate().onEvent {
                            transition {
                                terminationCallback.invoke(input)
                            }
                        }

                        if (input == 0) {
                            Action.onInit().onEvent {
                                // This is outside of effect to trigger termination while running
                                observer?.dispose()
                                none()
                            }
                        }
                    }
                )
            }
        }

        observer = root.test()
        observer.input(0)

        // No output since formula exited before producing an output
        observer.assertOutputCount(0)

        // Check that termination was called
        assertThat(terminationCallback.values()).containsExactly(0).inOrder()
    }

    @Test fun `runtime termination triggered by an effect`() {
        val terminationCallback = TestEventCallback<Int>()
        var observer: TestFormulaObserver<Int, *, *>? = null
        val root = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = input,
                    actions = context.actions {
                        Action.onTerminate().onEvent {
                            transition {
                                terminationCallback.invoke(input)
                            }
                        }

                        if (input == 0) {
                            Action.onInit().onEvent {
                                transition {
                                    observer?.dispose()
                                }
                            }
                        }
                    }
                )
            }
        }

        observer = root.test()
        observer.input(0)

        // No output since formula exited before producing an output
        observer.assertOutputCount(0)

        // Check that termination was called
        assertThat(terminationCallback.values()).containsExactly(0).inOrder()
    }

    @Test
    fun `multiple event updates`() {
        StartStopFormula().test().input(Unit)
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
    fun `no state changes after event action is removed`() {
        StartStopFormula().test().input(Unit)
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
        MultipleChildEvents.formula().test().input(Unit)
            .output { child.incrementAndMessage() }
            .output { child.incrementAndMessage() }
            .output { child.incrementAndMessage() }
            .output { assertThat(state).isEqualTo(3) }
    }

    @Test
    fun `transition after no re-evaluation pass`() {
        val sideEffectCallback = TestCallback()
        TransitionAfterNoEvaluationPass.formula(sideEffectCallback).test().input(Unit)
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
        ChildTransitionAfterNoEvaluationPass.formula(sideEffectCallback).test().input(Unit)
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
        NestedChildTransitionAfterNoEvaluationPass.formula(sideEffectCallback).test().input(Unit)
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
        MessageFormula().test().input(MessageFormula.Input(messageHandler = messageHandler))
            .output { triggerMessage() }
            .assertOutputCount(1) // no state change, so no re-evaluation
            .apply {
                assertThat(messageHandler.values()).hasSize(1)
            }
    }

    @Test
    fun `message after no re-evaluation pass`() {
        val messageHandler = TestEventCallback<Int>()
        MessageFormula().test().input(MessageFormula.Input(messageHandler = messageHandler))
            .output { triggerMessage() }
            .output { triggerMessage() }
            .assertOutputCount(1)
            .apply {
                assertThat(messageHandler.values()).hasSize(2)
            }
    }

    @Test
    fun `child message with no parent state change`() {
        ChildMessageNoParentStateChange.formula().test().input(Unit)
            .output { child.triggerMessage() }
            .assertOutputCount(1)  // no state change, so no re-evaluation
    }

    @Test
    fun `child message with parent state change`() {
        val inspector = CountingInspector()
        ChildMessageWithParentStateChange.formula().test(inspector = inspector).input(Unit)
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
        formula.test(inspector = inspector).input(Unit)
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
        formula.test(inspector = inspector).input(Unit)
            .output { assertThat(child.state).isEqualTo(3) }


        // TODO: run count could be reduced to 1 with inline effect execution
        inspector.assertRunCount(4)
        inspector.assertEvaluationCount(HasChildFormula::class, 8)
        inspector.assertEvaluationCount(OnInitActionFormula::class, 1)
    }

    @Test
    fun `child action triggers parent event on start`() {
        val increments = listOf(1, 1, 1, 1, 1, 1)
        ChildActionFiresParentEventOnStart.formula(runChildOnStart = true, increments).test()
            .input(value = Unit)
            .output { assertThat(value).isEqualTo(6) }
            .apply {
                // Efficiently we emit only emit 2 values
                assertThat(values()).hasSize(1)
            }

        ChildActionFiresParentEventOnStart.formula(
            runChildOnStart = false,
            increments
        ).test().input(value = Unit)
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
        formula.test(inspector = inspector).input(Unit)
            .apply {
                inspector.assertActionsStarted(1)
                inspector.assertStateTransitions(IncrementFormula::class, 4)
            }
            .output { assertThat(this).isEqualTo(4) }
    }

    @Test
    fun `side effect triggers parent state transition`() {
        ChildMessageTriggersEventTransitionInParent.formula().test().input(Unit)
            .output { child.triggerSideEffect() }
            .output {
                assertThat(count).isEqualTo(1)
            }
    }

    @Test
    fun `child start action triggers self and parent state changes`() {
        val inspector = CountingInspector()
        val formula = CombinedParentAndChildStateChange.formula()
        formula.test(inspector = inspector).input(Unit)
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
        formula.test(inspector = inspector).input(Unit)
            .output { child.onEvent() }
            .output {
                assertThat(state).isEqualTo(1)
                assertThat(child.state).isEqualTo(1)
            }

        inspector.assertRunCount(3)
        inspector.assertEvaluationCount(OnEventFormula::class, 2)
        inspector.assertEvaluationCount(HasChildFormula::class, 3)
    }

    @Test fun `transition effect queue maintains FIFO order when starting a new action during a transition`() {
        val inspector = CountingInspector()
        val onEvent = TestEventCallback<EffectOrderFormula.Event>()
        val initialInput = EffectOrderFormula.Input(onEvent = onEvent)
        EffectOrderFormula().test(inspector = inspector).input(initialInput)
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
        ChildStateResetAfterToggle.formula().test().input(Unit)
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
        MessageFormula().test().input(MessageFormula.Input(messageHandler = {}))
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
        EventCallbackFormula().test().input(Unit)
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
        formula.test().input(Unit)
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
        MessageFormula().test().input(MessageFormula.Input(messageHandler = {}))
            .output { incrementAndMessage() }
            .output { incrementAndMessage() }
            .assertOutputCount(3)
            .apply {
                assertThat(values().map { it.incrementAndMessage }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `event listeners are equal across render model changes`() {
        EventCallbackFormula().test().input(Unit)
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
        OptionalCallbackFormula().test().input(Unit)
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
    fun `dispatched event is ignored if listener was disabled before event is processed`() {
        data class State(
            val listenerEnabled: Boolean = true,
            val value: Int = 0,
        )

        data class Output(
            val value: Int,
            val increment: () -> Unit,
        )

        val disableListenerRelay = FlowRelay()
        val formula = object : Formula<Unit, State, Output>() {
            override fun initialState(input: Unit): State = State()

            override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
                val increment = if (state.listenerEnabled) {
                    context.callback {
                        transition(state.copy(value = state.value.inc()))
                    }
                } else {
                    context.callback { none() }
                }
                return Evaluation(
                    output = Output(
                        value = state.value,
                        increment = increment
                    ),
                    actions = context.actions {
                        disableListenerRelay.action().onEvent {
                            val listener = state.copy(listenerEnabled = false)
                            transition(listener)
                        }
                    }
                )
            }
        }

        val dispatcher = TestDispatcher()
        val observer = formula.test(dispatcher = dispatcher)

        // Initialize formula
        observer.input(Unit)
        dispatcher.executeAndClear()

        // First
        val increment = observer.values().last().increment
        disableListenerRelay.triggerEvent()
        increment()
        increment()
        increment()

        dispatcher.executeAndClear()
        observer.output { assertThat(value).isEqualTo(0) }
    }

    @Test
    fun `dispatching does not affect event order`() {
        var observer: TestFormulaObserver<Unit, OptionalCallbackFormula.Output, OptionalCallbackFormula>? =
            null
        FormulaPlugins.setPlugin(object : Plugin {
            override fun backgroundThreadDispatcher(): Dispatcher {
                return object : Dispatcher {
                    override fun dispatch(executable: () -> Unit) {
                        // We try to disable callback before processing increment
                        observer?.output { toggleCallback() }
                        executable()
                    }

                    override fun isDispatchNeeded(): Boolean {
                        return true
                    }
                }
            }
        })

        val root = OptionalCallbackFormula(
            incrementExecutionType = Transition.Background
        )
        observer = root.test().input(Unit)
        observer.output { listener?.invoke() }

        // Increment was processed before listener removal
        observer.output { assertThat(state).isEqualTo(1) }
    }

    @Test
    fun `listeners are not the same after removing then adding it again`() {
        OptionalCallbackFormula().test().input(Unit)
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
        OptionalEventCallbackFormula().test().input(Unit)
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
        OptionalEventCallbackFormula().test().input(Unit)
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
        val subject = ReusableFunctionCreatesUniqueListeners.test()
        subject.output { assertThat(firstListener).isNotEqualTo(secondListener) }
    }

    @Test
    fun `creating listener within a loop returns a unique listener`() {
        val subject = UniqueListenersWithinLoop.test()
        subject.output { assertThat(listeners).containsNoDuplicates() }
    }

    @Test
    fun `duplicate listener keys are handled by indexing`() {
        val subject = DuplicateListenerKeysHandledByIndexing.test()
        subject.output { assertThat(listeners).containsNoDuplicates() }
    }

    @Test
    fun `duplicate child formulas are handled by indexing`() {
        val childFormula = object : StatelessFormula<Int, Int>() {
            override fun key(input: Int): Any = input

            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = 1
                )
            }
        }

        val parentFormula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                val childCount = listOf(1, 1, 2, 1, 1).sumOf { key ->
                    context.child(childFormula, key)
                }
                return Evaluation(childCount)
            }
        }

        // Need failOnError to avoid test failing due to duplicate child errors
        parentFormula.test(failOnError = false).input(Unit).output {
            assertThat(this).isEqualTo(5)
        }
    }

    @Test
    fun `using key to scope listeners within another function`() {
        val formula = UsingKeyToScopeCallbacksWithinAnotherFunction.TestFormula()
        formula.test()
            .input(Unit)
            .assertOutputCount(1)
    }

    @Test
    fun `remove item from a list using a key block for each item`() {
        KeyUsingListFormula
            .test(items = listOf("one", "two", "three"))
            .output {
                items[1].onDeleteSelected()
            }
            .output { assertThat(items).hasSize(2) }
    }

    // Action test cases

    @Test fun `action event triggers a state change`() {
        val formula = StartStopFormula()
        formula.test().input(Unit)
            .output { this.startListening() }
            .output { assertThat(state).isEqualTo(0) }
            .apply { formula.incrementEvents.triggerEvent() }
            .output { assertThat(state).isEqualTo(1) }
    }

    @Test fun `action triggers only side-effects with no state change`() {
        val eventCallback = TestEventCallback<String>()
        val formula = OnlyUpdateFormula<Unit> {
            Action.fromFlow { flowOf("a", "b") }.onEvent {
                transition {
                    eventCallback(it)
                }
            }
        }

        val observer = formula.test().input(Unit)
        assertThat(observer.values()).containsExactly(Unit).inOrder()
        assertThat(eventCallback.values()).containsExactly("a", "b").inOrder()
    }

    @Test fun `action is disposed when evaluation does not contain it`() {
        DynamicStreamSubject()
            .updateStreams(keys = arrayOf("1"))
            .assertRunning(keys = arrayOf("1"))
            .updateStreams(keys = emptyArray())
            .assertRunning(keys = emptyArray())
    }

    @Test fun `action is removed when formula is removed`() {
        DynamicStreamSubject()
            .updateStreams(keys = arrayOf("1"))
            .assertRunning(keys = arrayOf("1"))
            .dispose()
            .assertRunning(keys = emptyArray())
    }

    @Test fun `action is reset when key changes`() {
        DynamicStreamSubject()
            .updateStreams("1")
            .assertRunning("1")
            .updateStreams("2")
            .assertRunning("2")
    }

    @Test
    fun `subscribes to updates before delivering messages`() {
        SubscribesToAllUpdatesBeforeDeliveringMessages
            .test()
            .output { assertThat(this).isEqualTo(4) }
    }

    @Test
    fun `multiple child worker updates`() {
        ChildStreamEvents()
            .startListening()
            .incrementBy(3)
            .assertCurrentValue(3)
    }

    @Test
    fun `child worker is removed`() {
        ChildStreamEvents()
            .startListening()
            .incrementBy(2)
            .stopListening()
            .incrementBy(4)
            .assertCurrentValue(2)
    }

    @Test fun `parent updates a child and self in a single action`() {
        val robot = ParentUpdateChildAndSelfOnEventRobot()
        robot.start()
        robot.subject.output { onAction() }
        robot.subject.output {
            assertThat(childValue).isEqualTo(1)
            assertThat(parentValue).isEqualTo(3)
        }
    }

    @Test
    fun `formula calls an event listener from a transition`() {
        val robot = NestedCallbackCallRobot()
        robot.start()
        robot.subject.output { onAction() }
        robot.subject.output { assertThat(value).isEqualTo(1) }
    }

    @Test
    fun `formula calls own event listener which starts multiple transitions`() {
        val robot = MultiChildIndirectStateChangeRobot()
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
        RunAgainActionFormula()
            .test(inspector = inspector)
            .input(Unit)
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
        StreamInitMessageDeliveredOnce.test().apply {
            assertThat(formula.timesInitializedCalled).isEqualTo(1)
        }
    }

    @Test
    fun `input changed message`() {
        StreamInputFormula().test()
            .input(0)
            .input(1)
            .input(2)
            .apply {
                assertThat(formula.messages).containsExactly(0, 1, 2).inOrder()
            }
    }

    @Test
    fun `events api ignores duplicate inputs`() {
        StreamInputFormula().test()
            .input(0)
            .input(0)
            .input(0)
            .input(0)
            .apply {
                assertThat(formula.messages).containsExactly(0).inOrder()
            }
    }

    @Test
    fun `remove all actions`() {
        DynamicStreamSubject()
            .updateStreams("one", "two", "three")
            .removeAll()
            .assertRunning(keys = emptyArray())
    }

    @Test
    fun `switch one action`() {
        DynamicStreamSubject()
            .updateStreams("one", "two", "three")
            .updateStreams("one", "three", "four")
    }

    @Test
    fun `action event listener is scoped to latest state`() {
        val events = listOf("a", "b")
        val formula = EventFormula(events)

        val inspector = CountingInspector()
        val expectedStates = listOf(1, 2)
        formula.test(inspector = inspector).input(Unit).apply {
            assertThat(formula.capturedStates()).isEqualTo(expectedStates)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(3)
    }

    @Test
    fun `action events are captured in order`() {
        val inspector = CountingInspector()
        val events = listOf("first", "second", "third", "third")
        val formula = EventFormula(events)
        formula.test(inspector = inspector).input(Unit).apply {
            assertThat(formula.capturedEvents()).isEqualTo(events)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(5)
    }

    @Test
    fun `action event listeners can handle at least 100k events`() {
        val inspector = CountingInspector()
        val eventCount = 100000
        val events = (1..eventCount).toList()
        val formula = EventFormula(events)
        formula.test(inspector = inspector).input(Unit)
            .apply {
                assertThat(values()).containsExactly(eventCount).inOrder()
            }

        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(100001)
    }

    @Test
    fun `child formula within multiple events on start`() {
        val inspector = CountingInspector()
        val eventCount = 100000
        val events = (1..eventCount).toList()
        val eventsFormula = EventFormula(events)
        val parent = HasChildFormula(eventsFormula)

        parent.test(inspector = inspector).input(Unit)
            .output { assertThat(child).isEqualTo(100000) }

        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(EventFormula::class, 100001)
        inspector.assertEvaluationCount(HasChildFormula::class, 1)
    }

    @Test
    fun `same action declarations are okay`() {
        val formula = OnlyUpdateFormula<Unit> {
            EmptyAction.init().onEvent {
                transition(Unit)
            }

            EmptyAction.init().onEvent {
                transition(Unit)
            }
        }

        formula.test().input(Unit)
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

        formula.test().input(Unit).assertOutputCount(1)
    }

    @Test
    fun `key is required when action is declared in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(1, 2, 3)
            list.forEach {
                EmptyAction.init().onEvent {
                    none()
                }
            }
        }

        val error = Try { formula.test().input(Unit) }.errorOrNull()
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `using key for action declared in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(1, 2, 3)
            list.forEach {
                EmptyAction.init(it).onEvent {
                    none()
                }
            }
        }

        formula.test().input(Unit).assertOutputCount(1)
    }

    @Test
    fun `multiple event actions without key`() {
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

        formula.test().input(Unit).apply {
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

        formula.test().input(1).apply {
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

        val error = Try { formula.test().input(Unit) }.errorOrNull()
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `terminate formula with multiple pending actions on first action init`() {
        val robot = PendingActionFormulaTerminatedOnActionInit()
        // Starts the formula
        robot.test.input(Unit)

        // Single action should be started
        robot.assertActionsStarted(1)

        // No output is emitted because we unsubscribe before doing so
        robot.test.assertOutputCount(0)
    }

    @Test
    fun `disposing formula triggers terminate message`() {
        TerminateFormula().test().input(Unit)
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
        formula.test().input(Unit)
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

        formula.test()
            .input(1)
            .input(2)
            .input(3)
            .dispose()
            .apply {
                assertThat(emissions).isEqualTo(1)
                assertThat(terminateCallback).isEqualTo(3)
            }
    }

    @Test
    fun `formula does not crash when action throws an exception during initialization`() {
        withPlugin(TestPlugin()) { plugin ->
            val formula = object : Formula<Int, Unit, Int>() {
                override fun initialState(input: Int): Unit = Unit

                override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                    return Evaluation(
                        output = input,
                        actions = context.actions {
                            RxAction.fromObservable<Unit> {
                                throw RuntimeException("Test exception")
                            }.onEvent {
                                none()
                            }
                        }
                    )
                }
            }

            formula.test(failOnError = false)
                .input(0)
                .assertOutputCount(1)
                .input(1)
                .assertOutputCount(2)
                .assertHasErrors()

            assertThat(plugin.errors).hasSize(1)
            assertThat(plugin.errors.first().error.message).contains("Test exception")
        }
    }

    @Test
    fun `formula does not crash when action throws an exception during termination`() {
        val action = object : Action<Unit> {
            override fun start(scope: CoroutineScope, emitter: Action.Emitter<Unit>): Cancelable {
                return Cancelable {
                    throw RuntimeException("Test exception")
                }
            }

            override fun key(): Any? = null
        }

        withPlugin(TestPlugin()) { plugin ->
            val formula = object : Formula<Int, Unit, Int>() {
                override fun initialState(input: Int): Unit = Unit

                override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                    return Evaluation(
                        output = input,
                        actions = context.actions {
                            if (input == 0) {
                                action.onEvent { none() }
                            }
                        }
                    )
                }
            }

            formula.test(failOnError = false)
                .input(0)
                .input(1)
                .input(2)
                .input(3)
                .assertOutputCount(4)
                .assertHasErrors()

            assertThat(plugin.errors).hasSize(1)
            assertThat(plugin.errors.first().error.message).contains("Test exception")
        }
    }

    @Test
    fun `formula does not crash when an effect throws an exception`() {
        withPlugin(TestPlugin()) { plugin ->
            val events = MutableSharedFlow<Int>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
            val formula = object : Formula<Unit, Int, Int>() {
                override fun initialState(input: Unit): Int = 0

                override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
                    return Evaluation(
                        output = state,
                        actions = context.actions {
                            Action.fromFlow { events }.onEvent {
                                transition(it) {
                                    throw IllegalStateException("Test exception $it")
                                }
                            }
                        }
                    )
                }
            }

            formula.test(failOnError = false)
                .input(Unit)
                .apply { events.tryEmit(1) }
                .apply { events.tryEmit(2) }
                .assertOutputCount(3)
                .assertHasErrors()

            assertThat(plugin.errors).hasSize(2)
            assertThat(plugin.errors[0].error.message).contains("Test exception 1")
            assertThat(plugin.errors[1].error.message).contains("Test exception 2")
        }
    }

    // End of action test cases

    // Child specific test cases

    @Test fun `child state change triggers parent formula evaluation`() {
        val childFormula = EventCallbackFormula()
        val formula = HasChildFormula(childFormula)
        formula.test().input(Unit)
            .output { child.changeState("new state") }
            .output { assertThat(child.state).isEqualTo("new state") }
    }

    @Test fun `child formula input change triggers an evaluation`() {
        val formula = DelegateFormula("default")
        formula.test().input(Unit)
            .output { changeChildInput("first") }
            .output { changeChildInput("second") }
            .apply {
                val expected = listOf("default", "first", "second")
                assertThat(values().map { it.childValue }).isEqualTo(expected)
            }
    }

    @Test
    fun `parent removes child when child emits a message`() {
        ChildRemovedOnMessage()
            .assertChildIsVisible(true)
            .closeByChildMessage()
            .assertChildIsVisible(false)
    }

    @Test fun `parent removes all child formulas`() {
        val formula = DynamicParentFormula()
        formula.test().input(Unit)
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

    @Test fun `child formulas with duplicate key are supported`() {
        val result = Try {
            val formula = DynamicParentFormula()
            formula.test(failOnError = false).input(Unit)
                .output { addChild(TestKey("1")) }
                .output { addChild(TestKey("1")) }
        }

        // No errors
        val error = result.errorOrNull()
        assertThat(error).isNull()
    }

    @Test fun `when child formulas with duplicate key are added, plugin is notified`() = withPlugin(TestPlugin()) {
        val result = Try {
            val formula = DynamicParentFormula()
            formula.test(failOnError = false).input(Unit)
                .output { addChild(TestKey("1")) }
                .output { addChild(TestKey("1")) }
        }

        // No errors
        val error = result.errorOrNull()
        assertThat(error).isNull()

        // Should log only once
        assertThat(it.errors).hasSize(1)

        val duplicateKeyError = it.errors.first() as FormulaError.ChildKeyAlreadyUsed
        assertThat(duplicateKeyError.formula).isEqualTo(DynamicParentFormula::class.java)
        assertThat(duplicateKeyError.error.key).isEqualTo(
            FormulaKey(null, KeyFormula::class.java, TestKey("1"))
        )
    }

    @Test
    fun `parent removal triggers childs terminate message`() {
        val terminateFormula = TerminateFormula()
        val formula = OptionalChildFormula(HasChildFormula(terminateFormula))

        formula.test().input(Unit).output { toggleChild() }.apply {
            assertThat(terminateFormula.timesTerminateCalled).isEqualTo(1)
        }
    }

    @Test
    fun `child formula termination triggers parent state transition`() {
        val relay = FlowRelay()
        val childFormula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        Action.onTerminate().onEvent {
                            relay.triggerEvent()
                            none()
                        }
                    }
                )
            }
        }

        val parentFormula = object : Formula<Boolean, Int, Int>() {
            override fun initialState(input: Boolean): Int = 0

            override fun Snapshot<Boolean, Int>.evaluate(): Evaluation<Int> {
                if (input) {
                    context.child(childFormula)
                }
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        relay.action().onEvent {
                            transition(state + 1)
                        }
                    }
                )
            }
        }

        val observer = parentFormula.test()
        observer.input(true)
        observer.output { assertThat(this).isEqualTo(0) }
        observer.input(false)
        observer.output { assertThat(this).isEqualTo(1) }
        observer.input(true)
        observer.input(false)
        observer.output { assertThat(this).isEqualTo(2) }
    }

    @Test
    fun `child formula termination triggers parent termination`() {
        var terminate = {}

        val childFormula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        Action.onTerminate().onEvent {
                            terminate()
                            none()
                        }
                    }
                )
            }
        }

        val parentFormula = object : StatelessFormula<Boolean, Int>() {
            override fun Snapshot<Boolean, Unit>.evaluate(): Evaluation<Int> {
                return if (input) {
                    context.child(childFormula)
                    Evaluation(1)
                } else {
                    Evaluation(0)
                }
            }
        }

        val observer = parentFormula.test()
        terminate = { observer.dispose() }
        observer.input(true)
        observer.assertOutputCount(1)
        observer.input(false)
        observer.assertOutputCount(1) // Terminated, should not have any more outputs
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
        formula.test().input(Unit).dispose()
        assertThat(terminateFormula.timesTerminateCalled).isEqualTo(10)
    }

    @Test fun `nested termination with input changed`() {
        NestedTerminationWithInputChanged().test()
            .input(false)
            .input(true)
            .input(false)
            .apply {
                assertThat(formula.terminateFormula.timesTerminateCalled).isEqualTo(1)
            }
    }

    @Test
    fun `canceling terminate action does not emit terminate message`() {
        val terminateCallback = TestCallback()
        RemovingTerminateStreamSendsNoMessagesFormula().test()
            .input(RemovingTerminateStreamSendsNoMessagesFormula.Input(onTerminate = terminateCallback))
            .input(RemovingTerminateStreamSendsNoMessagesFormula.Input(onTerminate = null))
            .apply {
                terminateCallback.assertTimesCalled(0)
            }
    }

    // TODO: I'm not sure if this is the right behavior
    @Test
    fun `action termination events are ignored`() {
        val formula = object : Formula<Boolean, Int, Int>() {
            override fun initialState(input: Boolean): Int = 0

            override fun Snapshot<Boolean, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        if (input) {
                            val action = object : Action<Unit> {
                                override fun start(
                                    scope: CoroutineScope,
                                    emitter: Action.Emitter<Unit>
                                ): Cancelable {
                                    return Cancelable {
                                        emitter.onEvent(Unit)
                                    }
                                }

                                override fun key(): Any? = null
                            }

                            action.onEvent {
                                transition(state + 1)
                            }
                        }
                    }
                )
            }
        }

        val observer = formula.test()
        observer.input(true)
        observer.input(false)
        observer.input(true)
        observer.input(false)
        observer.output { assertThat(this).isEqualTo(0) }
    }

    @Test
    fun `action triggers another transition on termination`() {
        val newRelay = FlowRelay()
        val formula = object : Formula<Boolean, Int, Int>() {
            override fun initialState(input: Boolean): Int = 0

            override fun Snapshot<Boolean, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        newRelay.action().onEvent {
                            transition(state + 1)
                        }

                        if (input) {
                            val action = object : Action<Unit> {
                                override fun start(
                                    scope: CoroutineScope,
                                    emitter: Action.Emitter<Unit>
                                ): Cancelable {
                                    return Cancelable {
                                        newRelay.triggerEvent()
                                    }
                                }

                                override fun key(): Any? = null
                            }

                            action.onEvent {
                                none()
                            }
                        }
                    }
                )
            }
        }

        val observer = formula.test()
        observer.input(true)
        observer.input(false)
        observer.input(true)
        observer.input(false)
        observer.output { assertThat(this).isEqualTo(2) }
    }

    @Test
    fun `action triggers formula termination during termination`() {
        var terminate = {}
        val formula = object : Formula<Boolean, Int, Int>() {
            override fun initialState(input: Boolean): Int = 0

            override fun Snapshot<Boolean, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        if (input) {
                            val action = object : Action<Unit> {
                                override fun start(
                                    scope: CoroutineScope,
                                    emitter: Action.Emitter<Unit>
                                ): Cancelable {
                                    return Cancelable {
                                        terminate()
                                    }
                                }

                                override fun key(): Any? = null
                            }

                            action.onEvent {
                                none()
                            }
                        }
                    }
                )
            }
        }

        val observer = formula.test()
        terminate = { observer.dispose() }
        observer.input(true)
        observer.input(false)
        observer.output { assertThat(this).isEqualTo(0) }
    }

    @Test fun `action events after full-termination are ignored`() {
        var sendCallback: (Unit) -> Unit = {}

        val formula = object : Formula<Boolean, Int, Int>() {
            override fun initialState(input: Boolean): Int = 0

            override fun Snapshot<Boolean, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        if (input) {
                            val action = object : Action<Unit> {
                                override fun key(): Any? = null

                                override fun start(
                                    scope: CoroutineScope,
                                    emitter: Action.Emitter<Unit>
                                ): Cancelable? {
                                    sendCallback = emitter::onEvent
                                    return null
                                }
                            }
                            action.onEvent {
                                transition(state + 1)
                            }
                        }
                    }
                )
            }
        }

        val observer = formula.test()
        observer.input(true)

        // Check that action runs
        sendCallback(Unit)
        observer.output { assertThat(this).isEqualTo(1) }

        // Cancel action
        observer.input(false)

        // Check that send actions are ignored
        sendCallback(Unit)
        sendCallback(Unit)
        sendCallback(Unit)
        observer.output { assertThat(this).isEqualTo(1) }
    }

    @Test
    fun `using from observable with input`() {
        val onItem = TestEventCallback<FromObservableWithInputFormula.Item>()
        FromObservableWithInputFormula().test()
            .input(FromObservableWithInputFormula.Input("1", onItem = onItem))
            .input(FromObservableWithInputFormula.Input("2", onItem = onItem))
            .apply {
                assertThat(onItem.values()).containsExactly(
                    FromObservableWithInputFormula.Item("1"),
                    FromObservableWithInputFormula.Item("2")
                ).inOrder()
            }
    }

    @Test fun `propagate error up when child emits error during initial evaluation`() {
        val child = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                throw IllegalStateException("evaluation error")
            }
        }

        val parent = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                val value = context.child(child)
                return Evaluation(output = value)
            }
        }

        parent.test(failOnError = false).input(Unit).apply {
            assertThat(values()).isEmpty()
            assertThat(errors()).hasSize(1)
        }
    }

    @Test fun `return last output when child emits an error during subsequent evaluation`() {
        val child = object : StatelessFormula<Int, Int>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                if (input == 1) throw IllegalStateException("evaluation error")
                return Evaluation(output = input)
            }
        }

        val parent = object : StatelessFormula<Int, Pair<Int, Int>>() {
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Pair<Int, Int>> {
                val childValue = context.child(child, input)
                return Evaluation(
                    output = Pair(input, childValue)
                )
            }
        }

        parent.test(failOnError = false).input(0).apply {
            // Initial emission, no errors
            output { assertThat(this).isEqualTo(Pair(0, 0)) }

            // Subsequent emission, with error
            input(1)
            assertThat(errors()).hasSize(1)
            output { assertThat(this).isEqualTo(Pair(1, 0)) }

            // Child is terminated, but parent still functions
            input(2)
            output { assertThat(this).isEqualTo(Pair(2, 0)) }
            assertThat(errors()).hasSize(1)
        }
    }

    @Test
    fun `emit error`() {
        val formula = OnlyUpdateFormula<Unit> {
            Action.onInit().onEvent {
                throw java.lang.IllegalStateException("crashed")
            }
        }
        val error = Try { formula.test().input(Unit) }.errorOrNull()
        assertThat(error?.message).isEqualTo("crashed")
    }

    @Test
    fun `errored child formula can fail in isolation when evaluation throws`() {
        val childFormula =
            object : StatelessFormula<HasChildrenFormula.ChildParamsInput<*>, Int>() {
                override fun Snapshot<HasChildrenFormula.ChildParamsInput<*>, Unit>.evaluate(): Evaluation<Int> {
                    if (input.index == 1) throw RuntimeException()
                    return Evaluation(output = input.index)
                }
            }
        val formula = HasChildrenFormula(childCount = 3, childFormula)
        val observer = formula.test(failOnError = false)
            .input(0)
            .output {
                assertThat(childOutputs).isEqualTo(listOf(0, 2))
            }

        assertThat(observer.errors()).hasSize(1)
    }

    @Test
    fun `errored child formula can fail in isolation when action throws`() {
        val childFormula =
            object : StatelessFormula<HasChildrenFormula.ChildParamsInput<*>, Int>() {
                override fun Snapshot<HasChildrenFormula.ChildParamsInput<*>, Unit>.evaluate(): Evaluation<Int> {
                    return Evaluation(
                        output = input.index,
                        actions = context.actions {
                            Action.onInit().onEvent {
                                if (input.index == 1) throw RuntimeException()
                                transition(Unit) {}
                            }
                        }
                    )
                }
            }
        val formula = HasChildrenFormula(childCount = 3, childFormula)
        val observer = formula.test(failOnError = false)
        observer
            .input(0)
            .output {
                assertThat(childOutputs).isEqualTo(listOf(0, 2))
            }

        assertThat(observer.errors()).hasSize(1)
    }

    @Test
    fun `errored child event listener disabled`() {
        val indexToExecutionCount = mutableMapOf<Int, Int>()
        val listener = { index: Int ->
            indexToExecutionCount[index] = indexToExecutionCount.getOrDefault(index, 0) + 1
        }
        val formula = HasChildrenFormula(
            childCount = 3,
            child = ChildErrorAfterToggleFormula(),
            createChildInput = { params ->
                HasChildrenFormula.ChildParamsInput(
                    index = params.index,
                    run = params.index,
                    value = callback { transition { listener(params.index) } }
                )
            },
        )
        formula.test(failOnError = false).input(0)
            .output {
                childOutputs.forEach { it.listener() }
                childOutputs[1].errorToggle()
                childOutputs.forEach { it.listener() }
            }
        val expected = mapOf(
            0 to 2,
            1 to 1,
            2 to 2,
        )
        assertThat(indexToExecutionCount).containsExactlyEntriesIn(expected)
    }

    @Test
    fun `initialize 100 levels nested formula`() {

        val inspector = CountingInspector()
        val formula = ExtremelyNestedFormula.nested(100)
        formula.test(inspector = inspector).input(Unit).output {
            assertThat(this).isEqualTo(100)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(200)
    }

    @Test
    fun `initialize 250 levels nested formula`() {
        val inspector = CountingInspector()
        val formula = ExtremelyNestedFormula.nested(250)
        formula.test(inspector = inspector).input(Unit).output {
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
        formula.test(inspector = inspector).input(Unit).output {
            assertThat(this).isEqualTo(500)
        }
        inspector.assertRunCount(1)
        inspector.assertEvaluationCount(1000)
    }

    @Test
    fun `mixing listener use with key use`() {
        val formula = MixingCallbackUseWithKeyUse.ParentFormula()
        formula.test().input(Unit).assertOutputCount(1)
    }

    @Test
    fun `nested keys are allowed`() {
        val subject = NestedKeyFormula().test().input(Unit)
        subject.assertNoErrors()
    }

    @Test
    fun `use key to scope child formula`() {
        val subject = UsingKeyToScopeChildFormula().test().input(Unit)
        subject.output {
            assertThat(children).containsExactly(
                "value 1", "value 2"
            ).inOrder()
        }
    }

    @Test
    fun `formula key is used to reset root formula state`() {
        RootFormulaKeyTestSubject()
            .assertValue(0)
            .increment()
            .increment()
            .assertValue(2)
            .resetKey()
            .assertValue(0)
    }

    @Test
    fun `formula multi-thread handoff to executing thread`() {
        with(MultiThreadRobot()) {
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
        with(MultiThreadRobot()) {
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
        with(MultiThreadRobot()) {
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
            override fun inspector(type: Class<*>): Inspector {
                return globalInspector
            }
        })

        val formula = StartStopFormula()
        val localInspector = TestInspector()
        val subject = formula.test(inspector = localInspector).input(Unit)
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
    fun `input changed inspector event`() {
        val localInspector = TestInspector()
        val globalInspector = TestInspector()
        FormulaPlugins.setPlugin(object : Plugin {
            override fun inspector(type: Class<*>): Inspector {
                return globalInspector
            }
        })

        val formula = object : StatelessFormula<Int, Int>() {
            val delegate = InputIdentityFormula<Int>()
            override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = context.child(delegate, input)
                )
            }
        }
        val subject = formula.test(inspector = localInspector).input(0)
        subject.input(1)

        for (inspector in listOf(globalInspector, localInspector)) {
            assertThat(inspector.events).containsExactly(
                "formula-run-started",
                "formula-started: null",
                "evaluate-started: null",
                "formula-started: com.instacart.formula.types.InputIdentityFormula",
                "evaluate-started: com.instacart.formula.types.InputIdentityFormula",
                "evaluate-finished: com.instacart.formula.types.InputIdentityFormula",
                "evaluate-finished: null",
                "formula-run-finished",

                "formula-run-started",
                "evaluate-started: null",
                "input-changed: null",
                "evaluate-started: com.instacart.formula.types.InputIdentityFormula",
                "input-changed: com.instacart.formula.types.InputIdentityFormula",
                "evaluate-finished: com.instacart.formula.types.InputIdentityFormula",
                "evaluate-finished: null",
                "formula-run-finished"
            ).inOrder()
        }
    }

    @Test
    fun `only global inspector events`() {
        val globalInspector = TestInspector()
        FormulaPlugins.setPlugin(object : Plugin {
            override fun inspector(type: Class<*>): Inspector {
                return globalInspector
            }
        })

        val formula = StartStopFormula()
        val subject = formula.test().input(Unit)
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

        val subject = formula.test().input(Unit)
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

        val subject = formula.test().input(Unit)
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

        val subject = formula.test().input(Unit)
        plugin.mainDispatcher.assertCalled(1)
        plugin.backgroundDispatcher.assertCalled(1)
    }

    @Test fun `use global background dispatcher`() {
        val globalDispatcher = IncrementingDispatcher()
        val plugin = TestDispatcherPlugin(defaultDispatcher = globalDispatcher)
        FormulaPlugins.setPlugin(plugin)

        val formula = IncrementFormula()
        val subject = formula.test().input(Unit)
        globalDispatcher.assertCalled(1) // Input
        subject.output { onIncrement() }
        globalDispatcher.assertCalled(2) // Input + event
    }

    @Test fun `specify formula-level dispatcher`() {
        val globalDispatcher = IncrementingDispatcher()
        val plugin = TestDispatcherPlugin(defaultDispatcher = globalDispatcher)
        FormulaPlugins.setPlugin(plugin)

        val formulaDispatcher = IncrementingDispatcher()
        val formula = IncrementFormula()
        val subject = formula.test(dispatcher = formulaDispatcher).input(Unit)
        globalDispatcher.assertCalled(0)
        formulaDispatcher.assertCalled(1) // Input
        subject.output { onIncrement() }
        globalDispatcher.assertCalled(0)
        formulaDispatcher.assertCalled(2) // Input + event
    }

    @Test fun `immediate execution type within callbackWithExecutionType overrides default dispatcher`() {
        val globalDispatcher = IncrementingDispatcher()
        val plugin = TestDispatcherPlugin(defaultDispatcher = globalDispatcher)
        FormulaPlugins.setPlugin(plugin)

        val formula = IncrementFormula(executionType = Transition.Immediate)
        val subject = formula.test().input(Unit)
        globalDispatcher.assertCalled(1) // Initial for input
        subject.output { onIncrement() }
        globalDispatcher.assertCalled(1) // Initial for input
    }

    @Test fun `immediate execution type within onEventWithExecutionType overrides default dispatcher`() {
        val globalDispatcher = IncrementingDispatcher()
        val plugin = TestDispatcherPlugin(defaultDispatcher = globalDispatcher)
        FormulaPlugins.setPlugin(plugin)

        val formula = EventCallbackFormula(executionType = Transition.Immediate)
        val subject = formula.test().input(Unit)
        globalDispatcher.assertCalled(1) // Initial for input
        subject.output { this.changeState("new state") }
        globalDispatcher.assertCalled(1)
    }

    @Test fun `background execution type within action overrides default dispatcher`() {
        val globalDispatcher = IncrementingDispatcher()
        val plugin = TestDispatcherPlugin(defaultDispatcher = globalDispatcher)
        FormulaPlugins.setPlugin(plugin)

        val relay = FlowRelay()
        val formula = object : Formula<Unit, Int, Int>() {
            override fun initialState(input: Unit): Int = 0

            override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        relay.action().onEventWithExecutionType(Transition.Background) {
                            transition(state + 1)
                        }
                    }
                )
            }
        }
        val subject = formula.test().input(Unit)
        globalDispatcher.assertCalled(1) // Once for input
        plugin.backgroundDispatcher.assertCalled(0)
        relay.triggerEvent()
        globalDispatcher.assertCalled(1) // Once for input
        plugin.backgroundDispatcher.assertCalled(1)
    }


    @Test fun `batched formulas are executed as part of a single evaluation`() {
        val childFormulaCount = 100

        val batchScheduler = StateBatchScheduler()
        val relay = FlowRelay()

        val childFormula = IncrementActionFormula(
            incrementRelay = relay,
            executionType = Transition.Batched(batchScheduler)
        )

        val rootFormula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                val sum = (0 until childFormulaCount).sumOf { id ->
                    context.key(id) {
                        context.child(childFormula, input)
                    }
                }
                return Evaluation(
                    output = sum,
                )
            }
        }

        val subject = rootFormula.test().input(Unit)
        batchScheduler.performUpdate {
            relay.triggerEvent()

            // No updates until batch scheduler executes the batch
            assertThat(subject.values()).containsExactly(0).inOrder()
        }

        // There are 100 formulas listening to the same relay, but only 1 evaluation happens.
        assertThat(subject.values()).containsExactly(0, 100).inOrder()

        // Second update
        batchScheduler.performUpdate { relay.triggerEvent() }

        // There are 100 formulas listening to the same relay, but only 1 evaluation happens.
        assertThat(subject.values()).containsExactly(0, 100, 200).inOrder()
    }

    @Test fun `two formulas using same state batch scheduler`() {
        val childFormulaCount = 100

        val batchScheduler = StateBatchScheduler()
        val relay = FlowRelay()

        val childFormula = IncrementActionFormula(
            incrementRelay = relay,
            executionType = Transition.Batched(batchScheduler)
        )

        val rootFormula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                val sum = (0 until childFormulaCount).sumOf { id ->
                    context.key(id) {
                        context.child(childFormula, input)
                    }
                }
                return Evaluation(
                    output = sum,
                )
            }
        }

        val subject1 = rootFormula.test().input(Unit)
        val subject2 = rootFormula.test().input(Unit)

        // Update
        batchScheduler.performUpdate { relay.triggerEvent() }

        // There are 100 formulas listening to the same relay, but only 1 evaluation happens.
        assertThat(subject1.values()).containsExactly(0, 100).inOrder()
        assertThat(subject2.values()).containsExactly(0, 100).inOrder()
     }

    @Test fun `batched state update while formula already is processing`() {
        val childFormulaCount = 100

        val batchScheduler = TestStateBatchScheduler()
        val incrementRelay = FlowRelay()
        val sleepRelay = FlowRelay()

        val childFormula = IncrementActionFormula(
            incrementRelay = incrementRelay,
            executionType = Transition.Batched(batchScheduler)
        )

        val rootFormula = object : Formula<Unit, Int, Int>() {
            override fun initialState(input: Unit): Int = 0

            override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
                val sum = (0 until childFormulaCount).sumOf { id ->
                    context.key(id) {
                        context.child(childFormula, input)
                    }
                }
                return Evaluation(
                    output = state + sum,
                    actions = context.actions {
                        sleepRelay.action().onEvent {
                            Thread.sleep(500)
                            transition(state + 1)
                        }
                    }
                )
            }
        }
        val subject = rootFormula.test().input(Unit)

        val countDownLatch = CountDownLatch(1)
        // Start sleep
        Executors.newSingleThreadExecutor().execute {
            sleepRelay.triggerEvent()

            countDownLatch.countDown()
        }

        // Wait for single thread to start the long evaluation (using sleep relay + sleep)
        Thread.sleep(100)

        batchScheduler.performUpdate(Unit) {
            incrementRelay.triggerEvent()
        }

        countDownLatch.await()

        // There are 100 formulas listening to the same relay, but only 1 evaluation happens.
        assertThat(subject.values()).containsExactly(0, 101).inOrder()

        assertThat(batchScheduler.batchesOutsideOfScope).hasSize(0)
    }

    @Test fun `batched events notify the inspector of start and stop`() {
        val globalInspector = TestInspector()
        FormulaPlugins.setPlugin(object : Plugin {
            override fun inspector(type: Class<*>): Inspector {
                return globalInspector
            }
        })
        val localInspector = TestInspector()

        val childFormulaCount = 3

        val batchScheduler = StateBatchScheduler()
        val relay = FlowRelay()

        val childFormula = IncrementActionFormula(
            incrementRelay = relay,
            executionType = Transition.Batched(batchScheduler)
        )

        val rootFormula = object : StatelessFormula<Unit, Int>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
                val sum = (0 until childFormulaCount).sumOf { id ->
                    context.key(id) {
                        context.child(childFormula, input)
                    }
                }
                return Evaluation(
                    output = sum,
                )
            }
        }

        val subject = rootFormula.test(inspector = localInspector).input(Unit)
        batchScheduler.performUpdate { relay.triggerEvent() }

        // Inspect!
        for (inspector in listOf(localInspector, globalInspector)) {
            // Filtering out logs before "batch-started"
            val events = inspector.events.dropWhile { !it.contains("batch-started") }
            assertThat(events).containsExactly(
                "batch-started: 3 updates",
                "state-changed: com.instacart.formula.types.IncrementActionFormula",
                "state-changed: com.instacart.formula.types.IncrementActionFormula",
                "state-changed: com.instacart.formula.types.IncrementActionFormula",
                "formula-run-started",
                "evaluate-started: null",
                "evaluate-started: com.instacart.formula.types.IncrementActionFormula",
                "evaluate-finished: com.instacart.formula.types.IncrementActionFormula",
                "evaluate-started: com.instacart.formula.types.IncrementActionFormula",
                "evaluate-finished: com.instacart.formula.types.IncrementActionFormula",
                "evaluate-started: com.instacart.formula.types.IncrementActionFormula",
                "evaluate-finished: com.instacart.formula.types.IncrementActionFormula",
                "evaluate-finished: null",
                "formula-run-finished",
                "batch-finished",
            ).inOrder()
        }
    }
}

