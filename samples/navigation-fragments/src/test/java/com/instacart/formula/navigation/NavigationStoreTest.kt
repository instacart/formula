package com.instacart.formula.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigationStoreTest {

    @Test
    fun `initial state should start with fragment 0`() {
        val store = NavigationStore()
        val initialState = store.getCurrentState()

        assertThat(initialState.currentFragmentId).isEqualTo(0)
        assertThat(initialState.navigationStack).containsExactly(0)
        assertThat(initialState.backStackFragments).isEmpty()
    }

    @Test
    fun `navigate to fragment should add to navigation stack`() {
        val store = NavigationStore()

        store.onEvent(NavigationEvent.NavigateToFragment(1))
        val state = store.getCurrentState()

        assertThat(state.currentFragmentId).isEqualTo(1)
        assertThat(state.navigationStack).containsExactly(0, 1).inOrder()
        assertThat(state.backStackFragments).containsExactly(0)
    }

    @Test
    fun `navigate back should remove from navigation stack`() {
        val store = NavigationStore()

        store.onEvent(NavigationEvent.NavigateToFragment(1))
        store.onEvent(NavigationEvent.NavigateToFragment(2))
        store.onEvent(NavigationEvent.NavigateBack)

        val state = store.getCurrentState()

        assertThat(state.currentFragmentId).isEqualTo(1)
        assertThat(state.navigationStack).containsExactly(0, 1).inOrder()
        assertThat(state.backStackFragments).containsExactly(0)
    }

    @Test
    fun `navigate back from root should stay at root`() {
        val store = NavigationStore()

        store.onEvent(NavigationEvent.NavigateBack) // Try to go back from root
        val state = store.getCurrentState()

        assertThat(state.currentFragmentId).isEqualTo(0)
        assertThat(state.navigationStack).containsExactly(0)
        assertThat(state.backStackFragments).isEmpty()
    }

    @Test
    fun `increment counter should emit counter increment event`() {
        val store = NavigationStore()
        val emittedIncrements = mutableListOf<Int>()

        store.counterIncrements.subscribe { fragmentId ->
            emittedIncrements.add(fragmentId)
        }

        store.onEvent(NavigationEvent.IncrementCounter(0))
        store.onEvent(NavigationEvent.IncrementCounter(1))
        store.onEvent(NavigationEvent.IncrementCounter(0))

        assertThat(emittedIncrements).containsExactly(0, 1, 0).inOrder()
    }

    @Test
    fun `complex navigation scenario`() {
        val store = NavigationStore()

        // Navigate forward: 0 -> 1 -> 2 -> 3
        store.onEvent(NavigationEvent.NavigateToFragment(1))
        store.onEvent(NavigationEvent.NavigateToFragment(2))
        store.onEvent(NavigationEvent.NavigateToFragment(3))

        // Navigate back: 3 -> 2
        store.onEvent(NavigationEvent.NavigateBack)

        val state = store.getCurrentState()

        assertThat(state.currentFragmentId).isEqualTo(2)
        assertThat(state.navigationStack).containsExactly(0, 1, 2).inOrder()
        assertThat(state.backStackFragments).containsExactly(0, 1).inOrder()
    }
}