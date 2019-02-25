package com.instacart.formula.integration

import arrow.core.Option
import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class FlowStoreTest {
    sealed class Key {
        object Main : Key()
        object Detail : Key()
    }

    lateinit var keys: BehaviorRelay<ActiveKeys<Key>>
    lateinit var store: FlowStore<Key>
    lateinit var mainScreenState: BehaviorRelay<String>
    lateinit var detailScreenState: BehaviorRelay<String>
    lateinit var subscriber: TestSubscriber<Option<KeyState<Key, *>>>

    @Before
    fun setup() {
        keys = BehaviorRelay.createDefault(ActiveKeys(emptyList()))
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

        subscriber = TestSubscriber()
        store.screen().subscribe(subscriber)

        keys.accept(ActiveKeys(listOf(Key.Main)))
        keys.accept(
            ActiveKeys(
                listOf(
                    Key.Main,
                    Key.Detail
                )
            )
        )
    }

    @After
    fun cleanUp() {
        subscriber.dispose()
    }

    @Test
    fun basicStateUpdates() {
        val values = subscriber.values()
        assertThat(values).containsExactlyElementsIn(expectedInitialState())
    }

    @Test
    fun emitOnlyCurrentScreenUpdates() {
        val expectedInitialState = expectedInitialState()
        assertThat(subscriber.values()).isEqualTo(expectedInitialState)

        mainScreenState.accept("main-update")

        // Should not have any more emissions
        assertThat(subscriber.values()).isEqualTo(expectedInitialState)
    }

    private fun expectedInitialState(): List<Option<KeyState<out Key, String>>> {
        return listOf(
            Option.empty(),
            Option.just(
                KeyState(
                    Key.Main,
                    "main-initial"
                )
            ),
            Option.empty(),
            Option.just(
                KeyState(
                    Key.Detail,
                    "detail-initial"
                )
            )

        )
    }
}
