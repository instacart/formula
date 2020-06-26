package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.streams.EmptyStream
import com.instacart.formula.test.TestCallback
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Ignore
import org.junit.Test

class FormulaRuntimeTest {

    @Test
    fun `multiple event updates`() {
        StreamFormula()
            .test()
            .renderModel { startListening() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply {
                val expected = listOf(0, 0, 1, 2, 3)
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test
    fun `no state changes after event stream is removed`() {
        StreamFormula()
            .test()
            .renderModel { startListening() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .renderModel { stopListening() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply { formula.incrementEvents.triggerIncrement() }
            .apply {
                val expected = listOf(0, 0, 1, 1)
                assertThat(values().map { it.state }).isEqualTo(expected)
            }
    }

    @Test
    fun `each child event handler should be scoped to latest state`() {
        MultipleChildEvents
            .formula()
            .test()
            .renderModel { child.incrementAndMessage() }
            .renderModel { child.incrementAndMessage() }
            .renderModel { child.incrementAndMessage() }
            .renderModel { assertThat(state).isEqualTo(3) }
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

    @Test
    fun `parent removes child when child emits a message`() {
        ChildRemovedOnMessage()
            .assertChildIsVisible(true)
            .closeByChildMessage()
            .assertChildIsVisible(false)
    }

    @Test
    fun `transition after no re-evaluation pass`() {
        val sideEffectCallback = TestCallback()
        TransitionAfterNoEvaluationPass
            .formula(sideEffectCallback)
            .test()
            .renderModel { triggerSideEffect() }
            .renderModel { triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                sideEffectCallback.assertTimesCalled(2)
            }
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        val sideEffectCallback = TestCallback()
        ChildTransitionAfterNoEvaluationPass
            .formula(sideEffectCallback)
            .test()
            .renderModel { child.triggerSideEffect() }
            .renderModel { child.triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                sideEffectCallback.assertTimesCalled(2)
            }
    }

    @Test
    fun `nested child transition after no re-evaluation pass`() {

        val sideEffectCallback = TestCallback()
        NestedChildTransitionAfterNoEvaluationPass
            .formula(sideEffectCallback)
            .test()
            .renderModel { child.child.triggerSideEffect() }
            .renderModel { child.child.triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                sideEffectCallback.assertTimesCalled(2)
            }
    }

    @Test
    fun `runtime emits messages`() {
        val messageHandler = TestEventCallback<Int>()
        MessageFormula()
            .test(MessageFormula.Input(messageHandler = messageHandler))
            .renderModel { triggerMessage() }
            .assertRenderModelCount(1) // no state change, so no re-evaluation
            .apply {
                assertThat(messageHandler.values()).hasSize(1)
            }
    }

    @Test
    fun `message after no re-evaluation pass`() {
        val messageHandler = TestEventCallback<Int>()
        MessageFormula()
            .test(MessageFormula.Input(messageHandler = messageHandler))
            .renderModel { triggerMessage() }
            .renderModel { triggerMessage() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(messageHandler.values()).hasSize(2)
            }
    }

    @Test
    fun `child message with no parent state change`() {
        ChildMessageNoParentStateChange
            .formula()
            .test()
            .renderModel { child.triggerMessage() }
            .assertRenderModelCount(1)  // no state change, so no re-evaluation
    }

    @Test
    fun `child message with parent state change`() {
        ChildMessageWithParentStateChange
            .formula()
            .test()
            .renderModel { child.triggerMessage() }
            .assertRenderModelCount(2)
            .renderModel { assertThat(state).isEqualTo(1) }
    }

    @Test
    fun `side effect triggers parent state transition`() {
        ChildMessageTriggersEventTransitionInParent
            .formula()
            .test()
            .renderModel { child.triggerSideEffect() }
            .renderModel {
                assertThat(count).isEqualTo(1)
            }
    }

    @Test
    fun `child state is reset after toggle`() {
        ChildStateResetAfterToggle
            .formula()
            .test()
            .renderModel { child!!.incrementAndMessage() }
            .renderModel { child!!.incrementAndMessage() }
            .renderModel { assertThat(child!!.state).isEqualTo(2) }
            .renderModel { toggleChild() }
            .renderModel { assertThat(child).isNull() }
            .renderModel { toggleChild() }
            .renderModel { assertThat(child!!.state).isEqualTo(0) }
    }

    @Test
    fun `multiple callbacks using the same render model`() {
        MessageFormula()
            .test(MessageFormula.Input(messageHandler = {}))
            .renderModel {
                incrementAndMessage()
                incrementAndMessage()
                incrementAndMessage()
            }
            .renderModel {
                assertThat(state).isEqualTo(3)
            }
    }

    @Test
    fun `multiple event callbacks using the same render model`() {
        EventCallbackFormula().test()
            .renderModel {
                changeState("one")
                changeState("two")
                changeState("three")
            }
            .renderModel { assertThat(state).isEqualTo("three") }
    }

    @Test
    fun `using a removed child callback should do nothing`() {
        OptionalChildFormula(MessageFormula()) {
            MessageFormula.Input(messageHandler = eventCallback { none() })
        }
            .test()
            .renderModel {
                val cachedChild = child!!
                toggleChild()

                cachedChild.incrementAndMessage()
                cachedChild.incrementAndMessage()
                cachedChild.incrementAndMessage()
            }
            .renderModel { assertThat(child).isNull() }
    }

    @Test
    fun `callbacks are equal across render model changes`() {
        MessageFormula()
            .test(MessageFormula.Input(messageHandler = {}))
            .renderModel { incrementAndMessage() }
            .renderModel { incrementAndMessage() }
            .assertRenderModelCount(3)
            .apply {
                assertThat(values().map { it.incrementAndMessage }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `event callbacks are equal across render model changes`() {
        EventCallbackFormula().test()
            .renderModel {
                changeState("one")
                changeState("two")
            }
            .assertRenderModelCount(3)
            .apply {
                assertThat(values().map { it.changeState }.toSet()).hasSize(1)
            }
    }

    @Test
    fun `removed callback is disabled`() {
        OptionalCallbackFormula()
            .test()
            .renderModel {

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
        OptionalCallbackFormula()
            .test()
            .renderModel {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.callback }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `removed event callback is disabled`() {
        OptionalEventCallbackFormula()
            .test()
            .renderModel {
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
        OptionalEventCallbackFormula()
            .test()
            .renderModel {
                toggleCallback()
                toggleCallback()
            }
            .apply {
                assertThat(values().map { it.callback }.toSet()).hasSize(3)
            }
    }

    @Test
    fun `using callbacks within another function crashes`() {
        UsingCallbacksWithinAnotherFunction
            .test()
            .assertError {
                it is IllegalStateException
            }
    }

    @Test
    fun `using key to scope callbacks within another function`() {
        UsingKeyToScopeCallbacksWithinAnotherFunction.TestFormula()
            .test()
            .assertRenderModelCount(1)
    }

    @Test
    fun `remove item from a list using a key block for each item`() {
        KeyUsingListFormula
            .test(items = listOf("one", "two", "three"))
            .renderModel {
                items[1].onDeleteSelected()
            }
            .renderModel { assertThat(items).hasSize(2) }
    }

    @Test
    fun `subscribes to updates before delivering messages`() {
        SubscribesToAllUpdatesBeforeDeliveringMessages
            .test()
            .renderModel {
                assertThat(this).isEqualTo(4)
            }
            .assertRenderModelCount(1)
    }

    @Test
    fun `init message executed once`() {
        StreamInitMessageDeliveredOnce.test().apply {
            assertThat(formula.timesInitializedCalled).isEqualTo(1)
        }
    }

    @Test
    fun `input changed message`() {
        StreamInputFormula()
            .test(input = Observable.range(0, 3))
            .apply {
                assertThat(formula.messages).containsExactly(0, 1, 2).inOrder()
            }
    }

    @Test
    fun `events api ignores duplicate inputs`() {
        StreamInputFormula()
            .test(input = Observable.just(0, 0, 0, 0))
            .apply {
                assertThat(formula.messages).containsExactly(0).inOrder()
            }
    }

    @Test
    fun `remove all streams`() {
        DynamicStreamSubject()
            .updateStreams("one", "two", "three")
            .removeAll()
    }

    @Test
    fun `switch one stream`() {
        DynamicStreamSubject()
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

        formula
            .test()
            .assertRenderModelCount(1)
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

        formula
            .test()
            .assertRenderModelCount(1)
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

        formula.start().test().assertError {
            it is IllegalStateException
        }
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

        formula.test().assertRenderModelCount(1)
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

        formula.test().apply {
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

        formula.test(1).apply {
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

        formula.start().test().assertError {
            it is IllegalStateException
        }
    }

    @Test
    fun `disposing formula triggers terminate message`() {
        TerminateFormula()
            .test()
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
        OptionalChildFormula(terminateFormula)
            .test()
            .apply {
                assertThat(terminateFormula.timesTerminateCalled).isEqualTo(0)
            }
            .renderModel { toggleChild() }
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

        formula
            .test(Observable.just(1, 2, 3))
            .dispose()
            .apply {
                assertThat(emissions).isEqualTo(1)
                assertThat(terminateCallback).isEqualTo(3)
            }
    }

    @Test
    fun `parent removal triggers childs terminate message`() {
        val terminateFormula = TerminateFormula()
        val formula = OptionalChildFormula(HasChildFormula(terminateFormula))

        formula.test().renderModel { toggleChild() }.apply {
            assertThat(terminateFormula.timesTerminateCalled).isEqualTo(1)
        }
    }

    @Test
    fun `multiple termination side-effects`() {
        val terminateFormula = TerminateFormula()
        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
                (0 until 10).forEach {
                    context.child(it, terminateFormula).input(Unit)
                }
                return Evaluation(Unit)
            }
        }
        formula.test().dispose()
        assertThat(terminateFormula.timesTerminateCalled).isEqualTo(10)
    }

    @Test fun `nested termination with input changed`() {
        NestedTerminationWithInputChanged()
            .test(Observable.just(false, true, false))
            .apply {
                assertThat(formula.terminateFormula.timesTerminateCalled).isEqualTo(1)
            }
    }

    @Test
    fun `canceling terminate stream does not emit terminate message`() {
        val terminateCallback = TestCallback()
        RemovingTerminateStreamSendsNoMessagesFormula()
            .test(
                input = Observable.just(
                    RemovingTerminateStreamSendsNoMessagesFormula.Input(onTerminate = terminateCallback),
                    RemovingTerminateStreamSendsNoMessagesFormula.Input(onTerminate = null)
                )
            )
            .apply {
                terminateCallback.assertTimesCalled(0)
            }
    }

    @Test
    fun `using from observable with input`() {
        val onItem = TestEventCallback<FromObservableWithInputFormula.Item>()
        FromObservableWithInputFormula()
            .test(
                input = Observable.just("1", "2").map {
                    FromObservableWithInputFormula.Input(it, onItem = onItem)
                }
            )
            .apply {
                assertThat(onItem.values()).containsExactly(
                    FromObservableWithInputFormula.Item("1"),
                    FromObservableWithInputFormula.Item("2")
                ).inOrder()
            }
    }

    @Test
    fun `initialize 100 levels nested formula`() {
        ExtremelyNestedFormula.nested(100).test().renderModel {
            assertThat(this).isEqualTo(100)
        }
    }

    @Test
    fun `initialize 250 levels nested formula`() {
        ExtremelyNestedFormula.nested(250).test().renderModel {
            assertThat(this).isEqualTo(250)
        }
    }

    @Ignore("stack overflows when there are 500 nested child formulas")
    @Test
    fun `initialize 500 levels nested formula`() {
        ExtremelyNestedFormula.nested(500).test().renderModel {
            assertThat(this).isEqualTo(500)
        }
    }

    @Test
    fun `mixing callback use with key use`() {
        MixingCallbackUseWithKeyUse.ParentFormula()
            .test()
            .assertRenderModelCount(1)
    }

    // TODO: maybe worth adding support eventually.
    @Test
    fun `nested keys are not allowed`() {
        NestedKeyFormula()
            .start()
            .test()
            .assertError { it is java.lang.IllegalStateException && it.message.orEmpty().startsWith("Nested scopes are not supported currently.") }
    }
}
