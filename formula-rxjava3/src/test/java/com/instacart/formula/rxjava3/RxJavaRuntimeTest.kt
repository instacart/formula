package com.instacart.formula.rxjava3

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.test
import com.instacart.formula.toFlow
import com.jakewharton.rxrelay3.PublishRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class RxJavaRuntimeTest {
    @Test fun `toFlow without an input`() {
        val formula = object : StatelessFormula<Unit, String>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = "value",
                )
            }
        }

        val observer = formula.toObservable().test()
        observer.assertValues("value")
        observer.assertNotComplete()
    }

    @Test fun `toFlow with a single input`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = input,
                )
            }
        }

        val observer = formula.toObservable("input").test()
        observer.assertValues("input")
        observer.assertNotComplete()
    }

    @Test fun `toFlow with input observable`() {
        val relay = PublishRelay.create<String>()
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = input,
                )
            }
        }
        val observer = formula.toObservable(relay).test()
        observer.assertValues()

        relay.accept("input-1")
        observer.assertValues("input-1")

        relay.accept("input-2")
        observer.assertValues("input-1", "input-2")

        observer.assertNotComplete()
    }

    @Test fun `nested launches`() {
        val sharedFlow = MutableSharedFlow<String>()
//        val relay = PublishRelay.create<String>()
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = input,
                )
            }
        }

        val parentFormula = object : Formula<Unit, String, String>() {
            override fun initialState(input: Unit): String = "initial"

            override fun Snapshot<Unit, String>.evaluate(): Evaluation<String> {

                return Evaluation(
                    output = state,
                    actions = context.actions {
                        // Nested `toObservable()` call
                        Action.fromFlow { formula.toFlow(sharedFlow) }.onEvent {
                            transition(it)
                        }
//                        RxAction.fromObservable { formula.toObservable(relay) }.onEvent {
//                            transition(it)
//                        }

                        // This should run after `formula.toFlow()` is ready
                        Action.onInit().onEvent {
                            // Verify that `toObservable` has subscribed to `relay`
//                            assertThat(relay.hasObservers()).isTrue()

                            sharedFlow.tryEmit("next")
//                            relay.accept("next")
                            none()
                        }
                    }
                )
            }
        }

        val observer = parentFormula.test().input(Unit) // toObservable(Unit).test()
        observer.output { assertThat(this).isEqualTo("next") }
//        observer.assertValues("initial", "next")
    }

    // Why does this work, while the other one doesn't?
    @Test fun `event orders`() {
        val events = mutableListOf<String>()
        val context = Dispatchers.Unconfined
        val scope = CoroutineScope(EmptyCoroutineContext)
        val job = scope.launch() {
            launch(context = context, start = CoroutineStart.UNDISPATCHED) {
                GlobalScope.launch(context = context, start = CoroutineStart.UNDISPATCHED) {
                    withContext(EmptyCoroutineContext) {
                        StaticFormula().toFlow(Unit).collect {
                            events.add("1")
                        }
                    }
                }
            }

            events.add("2")
        }

        runBlocking { job.join() }
        assertThat(events).isEqualTo(listOf("1", "2"))
    }

    @Test fun `event orders 2`() {
        val events = mutableListOf<String>()
        val context = Dispatchers.Unconfined
        val job = GlobalScope.launch(context = context, start = CoroutineStart.UNDISPATCHED) {
            val scope = this
            launch {
                flowOf("input").collect {
                    scope.launch(context = context, start = CoroutineStart.UNDISPATCHED) {
                        flowOf("action").collect {
                            events.add("1")
                        }
                    }
                }
            }
        }
        events.add("2")

        runBlocking { job.join() }
        assertThat(events).isEqualTo(listOf("1", "2"))
    }

    class StaticFormula : StatelessFormula<Unit, String>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<String> {
            return Evaluation(output = "initial")
        }
    }
}