package com.instacart.formula.navigation

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentState
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

class NavigationActivityFormulaTest {

    @Test
    fun `on init navigates to fragment 0`() = runTest {
        val fragmentStateFlow = MutableSharedFlow<FragmentState>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
        val actions = mutableListOf<NavigationAction>()

        val observer = NavigationActivityFormula(fragmentStateFlow)
            .test()
            .input(NavigationActivityFormula.Input(onNavigation = { actions.add(it) }))

        // Ensure the formula has produced at least one output to flush init actions
        observer.output { /* no-op */ }

        assertThat(actions).contains(NavigationAction.NavigateToFragment(0))

        observer.dispose()
    }

    @Test
    fun `navigation stack updates from fragment state flow`() = runTest {
        val fragmentStateFlow = MutableSharedFlow<FragmentState>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
        val observer = NavigationActivityFormula(fragmentStateFlow)
            .test()
            .input(NavigationActivityFormula.Input(onNavigation = { }))

        val stackFlow = observer.values().last().navigationStack

        val active = listOf(
            FragmentId(instanceId = "i0", key = CounterFragmentKey(0)),
            FragmentId(instanceId = "i1", key = CounterFragmentKey(1)),
            FragmentId(instanceId = "i2", key = CounterFragmentKey(2)),
        )

        fragmentStateFlow.tryEmit(FragmentState(activeIds = active))

        val stack = stackFlow.first()
        assertThat(stack).isEqualTo(listOf(0, 1, 2))

        observer.dispose()
    }

    @Test
    fun `on navigate to next uses 1 when navigating forward for the first time`() = runTest {
        val fragmentStateFlow = MutableSharedFlow<FragmentState>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
        val actions = mutableListOf<NavigationAction>()

        val observer = NavigationActivityFormula(fragmentStateFlow)
            .test()
            .input(NavigationActivityFormula.Input(onNavigation = { actions.add(it) }))

        // Invoke navigate to next when there is no current fragment state
        observer.output { onNavigateToNext() }

        // We expect initial navigate to 0 and then next to 1
        assertThat(actions).containsExactly(
            NavigationAction.NavigateToFragment(0),
            NavigationAction.NavigateToFragment(1),
        )

        observer.dispose()
    }

    @Test
    fun `on navigate to next uses max+1 from current fragment state`() = runTest {
        val fragmentStateFlow = MutableSharedFlow<FragmentState>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
        val actions = mutableListOf<NavigationAction>()

        val observer = NavigationActivityFormula(fragmentStateFlow)
            .test()
            .input(NavigationActivityFormula.Input(onNavigation = { actions.add(it) }))

        // Provide a current fragment state with ids 0,1,2
        val active = listOf(
            FragmentId(instanceId = "i0", key = CounterFragmentKey(0)),
            FragmentId(instanceId = "i1", key = CounterFragmentKey(1)),
            FragmentId(instanceId = "i2", key = CounterFragmentKey(2)),
        )
        fragmentStateFlow.tryEmit(FragmentState(activeIds = active))

        // Trigger navigate to next -> expect 3
        observer.output { onNavigateToNext() }

        assertThat(actions).hasSize(2) // initial fragment 0 + fragment 3
        assertThat(actions.last()).isEqualTo(NavigationAction.NavigateToFragment(3))

        observer.dispose()
    }

    @Test
    fun `on navigate back emits action`() = runTest {
        val fragmentStateFlow = MutableSharedFlow<FragmentState>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
        val actions = mutableListOf<NavigationAction>()

        val observer = NavigationActivityFormula(fragmentStateFlow)
            .test()
            .input(NavigationActivityFormula.Input(onNavigation = { actions.add(it) }))

        observer.output { onNavigateBack() }

        assertThat(actions.last()).isEqualTo(NavigationAction.NavigateBack)

        observer.dispose()
    }

    @Test
    fun `counter increments flow emits when increment is called`() = runTest {
        val fragmentStateFlow = MutableSharedFlow<FragmentState>(replay = 0, extraBufferCapacity = Int.MAX_VALUE)
        val observer = NavigationActivityFormula(fragmentStateFlow)
            .test()
            .input(NavigationActivityFormula.Input(onNavigation = { }))

        val incrementsFlow = observer.values().last().counterIncrements
        val firstIncrement = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(1_000) { incrementsFlow.first() } }

        observer.output { onIncrementCounter(42) }

        assertThat(firstIncrement.await()).isEqualTo(42)

        observer.dispose()
    }
}
