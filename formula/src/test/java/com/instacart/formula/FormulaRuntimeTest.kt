package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.messages.TestEventCallback
import com.instacart.formula.streams.EmptyStream
import com.instacart.formula.test.test
import io.reactivex.Observable
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
                assertThat(values().map { it.state }).containsExactly(0, 0, 1, 2, 3)
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
                assertThat(values().map { it.state }).containsExactly(0, 0, 1, 1)
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
        val sideEffectService = SideEffectService()
        TransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { triggerSideEffect() }
            .renderModel { triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        ChildTransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { child.triggerSideEffect() }
            .renderModel { child.triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `nested child transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        NestedChildTransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { child.child.triggerSideEffect() }
            .renderModel { child.child.triggerSideEffect() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
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
        SideEffectTriggersParentTransition
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
                assertThat(values().map { it.state }).containsExactly(0, 1, 1)
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
                assertThat(values().map { it.state }).containsExactly(0, 1, 1)
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
    fun `effect without input executed once`() {
        EffectExecutedOnce.test().apply {
            assertThat(formula.effect).isEqualTo(1)
        }
    }

    @Test
    fun `effects with input`() {
        EffectWithInputFormula()
            .test(input = Observable.range(0, 3))
            .apply {
                assertThat(formula.effects).containsExactly(0, 1, 2)
            }
    }

    @Test
    fun `effect api ignores duplicate inputs`() {
        EffectWithInputFormula()
            .test(input = Observable.just(0, 0, 0, 0))
            .apply {
                assertThat(formula.effects).containsExactly(0)
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
            events(EmptyStream()) {
                transition(Unit)
            }

            events(EmptyStream()) {
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
            events("same", Observable.just(1)) {
                none()
            }

            events("same", Observable.just(1)) {
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
                events(EmptyStream()) {
                    none()
                }
            }
        }

        formula.state(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    @Test
    fun `using key for stream declared in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(1, 2, 3)
            list.forEach {
                events("$it", EmptyStream()) {
                    none()
                }
            }
        }

        formula.test().assertRenderModelCount(1)
    }

    @Test
    fun `multiple effects without key`() {
        var executed = 0
        val formula = OnlyUpdateFormula<Unit> {
            effect {
                executed += 1
            }

            effect {
                executed += 1
            }
        }

        formula.test().apply {
            assertThat(executed).isEqualTo(2)
        }
    }

    @Test
    fun `multiple effects with input and without key`() {
        var executed = 0
        val formula = OnlyUpdateFormula<Int> {
            effect(it) {
                executed += 1
            }

            effect(it) {
                executed += 1
            }
        }

        formula.test(1).apply {
            assertThat(executed).isEqualTo(2)
        }
    }

    @Test
    fun `key is required for effects in a loop`() {
        val formula = OnlyUpdateFormula<Unit> {
            val list = listOf(0, 1, 2)
            list.forEach {
                effect {
                    // nothing needed here
                }
            }
        }

        formula.state(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    @Test
    fun `cancellation effect by unsubscribing`() {
        CancellationEffectFormula()
            .test()
            .apply {
                assertThat(formula.timesCancelledCalled).isEqualTo(0)
            }
            .dispose()
            .apply {
                assertThat(formula.timesCancelledCalled).isEqualTo(1)
            }
    }

    @Test
    fun `cancellation by removing child formula`() {
        val cancellationFormula = CancellationEffectFormula()
        OptionalChildFormula(cancellationFormula)
            .test()
            .apply {
                assertThat(cancellationFormula.timesCancelledCalled).isEqualTo(0)
            }
            .renderModel { toggleChild() }
            .apply {
                assertThat(cancellationFormula.timesCancelledCalled).isEqualTo(1)
            }
    }

    @Test
    fun `cancellation is scoped to latest input`() {
        var emissions = 0
        var cancelCallback = -1
        val formula = OnlyUpdateFormula<Int> { input ->
            cancellationMessage {
                emissions += 1
                cancelCallback = input
            }
        }

        formula
            .test(Observable.just(1, 2, 3))
            .dispose()
            .apply {
                assertThat(emissions).isEqualTo(1)
                assertThat(cancelCallback).isEqualTo(3)
            }
    }
}
