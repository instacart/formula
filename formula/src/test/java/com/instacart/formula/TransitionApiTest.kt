package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.internal.DelegateTransitionContext
import com.instacart.formula.internal.toResult
import com.instacart.formula.test.TestListener
import org.junit.Test

class TransitionApiTest {

    @Test fun `none transition`() {
        val transition = Transition<Int, Unit> { none() }
        val result = transition.toResult(FakeTransitionContext(0), Unit)
        Truth.assertThat(result).isEqualTo(Transition.Result.None)
    }

    @Test fun `stateful transition`() {
        val transition = Transition<Int, Unit> { transition(state + 1) }
        val result = transition.toResult(FakeTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(1)
    }

    @Test fun `stateful transition with effects`() {
        val testListener = TestListener<Unit>()
        val transition = Transition<Int, Unit> {
            transition(state + 1) {
                testListener.invoke(Unit)
            }
        }

        val result = transition.toResult(FakeTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(1)

        result.assertAndExecuteEffects()
        testListener.assertTimesCalled(1)
    }

    @Test fun `only effects transitions`() {
        val testListener = TestListener<Unit>()
        val transition = Transition<Int, Unit> {
            transition {
                testListener.invoke(Unit)
            }
        }

        val result = transition.toResult(FakeTransitionContext(0), Unit)
        result.assertAndExecuteEffects()
        testListener.assertTimesCalled(1)
    }

    @Test
    fun `transition delegates to another transition`() {
        val transition = Transition<Int, Unit> {
            delegate(AddTransition(), 5)
        }
        val result = transition.toResult(FakeTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(5)
    }

    @Test fun `transition combine none transition with stateful transition`() {
        val transition = Transition<Int, Unit> {
            none().andThen(AddTransition(), 1)
        }

        val result = transition.toResult(DelegateTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(1)
    }

    @Test fun `transition combine only effects transition with stateful transition`() {
        val testListener = TestListener<Unit>()
        val transition = Transition<Int, Unit> {
            val result = transition { testListener.invoke(Unit) }
            result.andThen(AddTransition(), 1)
        }

        val result = transition.toResult(DelegateTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(1)

        result.assertAndExecuteEffects()
        testListener.assertTimesCalled(1)
    }

    @Test fun `transition combine stateful transition with another transition`() {
        val transition = Transition<Int, Unit> {
            transition(state + 1).andThen(AddTransition(), 1)
        }

        val result = transition.toResult(DelegateTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(2)
    }

    @Test fun `transition combine keeps effect order`() {
        val listener = TestListener<Int>()
        val incrementAndNotify = IncrementAndNotifyTransition(listener)
        val transition = Transition<Int, Unit> {
            delegate(incrementAndNotify).andThen(incrementAndNotify)
        }

        val result = transition.toResult(DelegateTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(2)

        result.assertAndExecuteEffects()
        Truth.assertThat(listener.values()).containsExactly(1, 2).inOrder()
    }

    @Test fun `state andThen another stateful result`() {
        val transition = Transition<Int, Unit> {
            (state + 1).andThen(AddTransition(), 2)
        }

        val result = transition.toResult(DelegateTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(3)
    }

    @Test fun `state andThen empty result`() {
        val transition = Transition<Int, Unit> {
            val empty = Transition<Int, Unit> { none() }
            (state + 1).andThen(empty)
        }

        val result = transition.toResult(DelegateTransitionContext(0), Unit).assertStateful()
        Truth.assertThat(result.state).isEqualTo(1)
    }

    private fun <State> Transition.Result<State>.assertStateful(): Transition.Result.Stateful<State> {
        Truth.assertThat(this).isInstanceOf(Transition.Result.Stateful::class.java)
        return this as Transition.Result.Stateful<State>
    }

    private fun <State> Transition.Result<State>.assertAndExecuteEffects() {
        Truth.assertThat(effects).isNotNull()
        effects?.execute()
    }

    class AddTransition : Transition<Int, Int> {
        override fun TransitionContext<Int>.toResult(event: Int): Transition.Result<Int> {
            return transition(state + event)
        }
    }

    class IncrementAndNotifyTransition(private val listener: Listener<Int>): Transition<Int, Unit> {
        override fun TransitionContext<Int>.toResult(event: Unit): Transition.Result<Int> {
            val newState = state + 1
            return transition(newState) {
                listener.invoke(newState)
            }
        }
    }

    class FakeTransitionContext<State>(override val state: State) : TransitionContext<State>
}