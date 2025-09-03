package com.instacart.formula.navigation

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Test

class CounterFragmentFormulaTest {

    @Test
    fun `initial state`() {
        val deps = TestDependencies()
        CounterFragmentFormula(deps)
            .test()
            .input(CounterFragmentFormula.Input(counterIndex = 1))
            .output {
                assertThat(fragmentId).isEqualTo(1)
                assertThat(counter).isEqualTo(0)
                assertThat(backStackFragments).isEmpty()
            }
    }

    @Test
    fun `increments only for matching fragment id`() {
        val deps = TestDependencies()
        val observer = CounterFragmentFormula(deps)
            .test()
            .input(CounterFragmentFormula.Input(counterIndex = 42))

        // Non-matching id should be ignored
        deps.counterIncrementsFlow.tryEmit(99)
        observer.output {
            assertThat(counter).isEqualTo(0)
        }

        // Matching id increments
        deps.counterIncrementsFlow.tryEmit(42)
        observer.output {
            assertThat(counter).isEqualTo(1)
        }

        // Multiple increments
        repeat(3) { deps.counterIncrementsFlow.tryEmit(42) }
        observer.output {
            assertThat(counter).isEqualTo(4)
        }
    }

    @Test
    fun `navigation stack updates from flow`() {
        val deps = TestDependencies()
        val observer = CounterFragmentFormula(deps)
            .test()
            .input(CounterFragmentFormula.Input(counterIndex = 3))

        val stack = listOf(1, 2, 3)
        deps.navigationStackFlow.tryEmit(stack)

        observer.output {
            assertThat(backStackFragments).isEqualTo(stack)
        }
    }

    @Test
    fun `on navigate to next invokes dependency`() {
        val deps = TestDependencies()
        val observer = CounterFragmentFormula(deps)
            .test()
            .input(CounterFragmentFormula.Input(counterIndex = 7))

        observer.output { onNavigateToNext() }
        assertThat(deps.navigateToNextCalled).isTrue()
    }

    @Test
    fun `on navigate back invokes dependency`() {
        val deps = TestDependencies()
        val observer = CounterFragmentFormula(deps)
            .test()
            .input(CounterFragmentFormula.Input(counterIndex = 7))

        observer.output { onNavigateBack() }
        assertThat(deps.navigateBackCalled).isTrue()
    }

    @Test
    fun `on increment counter invokes dependency and increments counter`() {
        val deps = TestDependencies()
        val observer = CounterFragmentFormula(deps)
            .test()
            .input(CounterFragmentFormula.Input(counterIndex = 7))

        observer.output { onIncrementCounter(7) }
        observer.output {
            assertThat(counter).isEqualTo(1)
        }
        assertThat(deps.lastIncrementedFragmentId).isEqualTo(7)
    }

    private class TestDependencies : CounterFragmentFormula.Dependencies {
        val navigationStackFlow = MutableSharedFlow<List<Int>>(replay = 0, extraBufferCapacity = 16)
        val counterIncrementsFlow = MutableSharedFlow<Int>(replay = 0, extraBufferCapacity = 16)

        override val navigationStack = navigationStackFlow
        override val counterIncrements = counterIncrementsFlow

        var navigateToNextCalled: Boolean = false
        var navigateBackCalled: Boolean = false
        var lastIncrementedFragmentId: Int? = null

        override val onNavigateToNext: () -> Unit = {
            navigateToNextCalled = true
        }

        override val onNavigateBack: () -> Unit = {
            navigateBackCalled = true
        }

        override val onIncrementCounter: (Int) -> Unit = { id ->
            lastIncrementedFragmentId = id
            // Simulate real behavior by emitting into the increments stream
            counterIncrementsFlow.tryEmit(id)
        }
    }
}
