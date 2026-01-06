package com.instacart.formula.navigation

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CounterFragmentFormulaTest {

    @Test
    fun `initial state`() = runTest {
        val store = CounterStore()
        val router = TestRouter()
        CounterFragmentFormula(store, router)
            .test(coroutineContext)
            .input(CounterFragmentFormula.Input(counterIndex = 1))
            .output {
                assertThat(fragmentId).isEqualTo(1)
                assertThat(counter).isEqualTo(0)
                assertThat(backStackFragments).isEmpty()
            }
    }

    @Test
    fun `increments only for matching fragment id`() = runTest {
        val store = CounterStore()
        val router = TestRouter()
        val observer = CounterFragmentFormula(store, router)
            .test(coroutineContext)
            .input(CounterFragmentFormula.Input(counterIndex = 42))

        // Non-matching id should be ignored
        store.incrementCounterFor(99)
        observer.output {
            assertThat(counter).isEqualTo(0)
        }

        // Matching id increments
        store.incrementCounterFor(42)
        observer.output {
            assertThat(counter).isEqualTo(1)
        }

        // Multiple increments
        repeat(3) { store.incrementCounterFor(42) }
        observer.output {
            assertThat(counter).isEqualTo(4)
        }
    }

    @Test
    fun `navigation stack updates from flow`() = runTest {
        val store = CounterStore()
        val router = TestRouter()
        val observer = CounterFragmentFormula(store, router)
            .test(coroutineContext)
            .input(CounterFragmentFormula.Input(counterIndex = 3))

        val stack = listOf(1, 2, 3)
        store.updateCounterStack(stack)

        observer.output {
            assertThat(backStackFragments).isEqualTo(stack)
        }
    }

    @Test
    fun `on navigate to next invokes router`() = runTest {
        val store = CounterStore()
        val router = TestRouter()
        val observer = CounterFragmentFormula(store, router)
            .test(coroutineContext)
            .input(CounterFragmentFormula.Input(counterIndex = 7))

        observer.output { onNavigateToNext() }
        assertThat(router.navigateToNextCalled).isTrue()
    }

    @Test
    fun `on navigate back invokes router`() = runTest {
        val store = CounterStore()
        val router = TestRouter()
        val observer = CounterFragmentFormula(store, router)
            .test(coroutineContext)
            .input(CounterFragmentFormula.Input(counterIndex = 7))

        observer.output { onNavigateBack() }
        assertThat(router.navigateBackCalled).isTrue()
    }

    @Test
    fun `on increment counter invokes counter store and increments counter`() = runTest {
        val store = CounterStore()
        val router = TestRouter()
        val observer = CounterFragmentFormula(store, router)
            .test(coroutineContext)
            .input(CounterFragmentFormula.Input(counterIndex = 7))

        observer.output { onIncrementCounter(7) }
        observer.output {
            assertThat(counter).isEqualTo(1)
        }
    }

    private class TestRouter : CounterRouter {
        var navigateToNextCalled: Boolean = false
        var navigateBackCalled: Boolean = false

        override fun onNavigateBack() {
            navigateBackCalled = true
        }

        override fun onNavigateToNext(nextCounterIndex: Int) {
            navigateToNextCalled = true
        }
    }
}
