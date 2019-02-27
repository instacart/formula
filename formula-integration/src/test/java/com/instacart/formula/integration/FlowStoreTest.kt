package com.instacart.formula.integration

import arrow.core.Option
import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test

class FlowStoreTest {
    sealed class Key {
        object Main : Key()
        object Detail : Key()
    }

    lateinit var keys: BehaviorRelay<BackStack<Key>>
    lateinit var store: FlowStore<Key>
    lateinit var mainScreenState: BehaviorRelay<String>
    lateinit var detailScreenState: BehaviorRelay<String>
    lateinit var subscriber: TestSubscriber<FlowState<Key>>

    @Before
    fun setup() {
        keys = BehaviorRelay.createDefault(BackStack(emptyList()))
        mainScreenState = BehaviorRelay.createDefault("main-initial")
        detailScreenState = BehaviorRelay.createDefault("detail-initial")

        store = FlowStore.init(keys.toFlowable(BackpressureStrategy.LATEST)) {
            register(Key.Main::class, init = {
                mainScreenState.toFlowable(BackpressureStrategy.LATEST)
            })

            register(Key.Detail::class, init = {
                detailScreenState.toFlowable(BackpressureStrategy.LATEST)
            })
        }

        subscriber = store.state().test()
    }

    @Test
    fun `single screen update`() {
        init()
            .apply {
                updateBackstack(Key.Main)
            }
            .assertValues(
                expectedState(),
                expectedState(Key.Main to null),
                expectedState(Key.Main to "main-initial")
            )
    }

    @Test fun `pop backstack updates state`() {
        init()
            .apply {
                updateBackstack(Key.Main)
                updateBackstack(Key.Main, Key.Detail)
                updateBackstack(Key.Main)
            }
            .assertValues(
                expectedState(),
                expectedState(Key.Main to null),
                expectedState(Key.Main to "main-initial"),
                expectedState(Key.Main to "main-initial", Key.Detail to null),
                expectedState(Key.Main to "main-initial", Key.Detail to "detail-initial"),
                expectedState(Key.Main to "main-initial")
            )
    }

    @Test
    fun `last entry always points to last backstack entry`() {
        init()
            .apply {
                updateBackstack(Key.Main)
                updateBackstack(Key.Main, Key.Detail)

                mainScreenState.accept("main-update")
            }
            .values()
            .last()
            .apply {
                assertThat(lastEntry()).isEqualTo(KeyState(Key.Detail, "detail-initial"))
            }
    }

    private fun updateBackstack(vararg entries: Key) {
        keys.accept(BackStack(entries.toList()))
    }

    private fun init(): TestSubscriber<FlowState<Key>> {
        return store.state().test()
    }

    private fun expectedState(vararg states: Pair<Key, *>): FlowState<Key> {
        val asList = states.toList()
        val keyStates = asList.fold(mutableMapOf<Key, KeyState<Key, *>>()) { acc, value ->
            if (value.second != null) {
                acc.put(value.first, KeyState(value.first, value.second))
            }

            acc
        }

        return FlowState(
            backStack = BackStack(asList.map { it.first }),
            states = keyStates
        )
    }
}
