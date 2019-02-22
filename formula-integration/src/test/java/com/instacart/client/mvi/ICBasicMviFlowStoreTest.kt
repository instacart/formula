package com.instacart.client.mvi

import arrow.core.Option
import com.google.common.truth.Truth.assertThat
import com.instacart.client.core.rx.toFlowable
import com.instacart.formula.ICMviState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class ICBasicMviFlowStoreTest {
    sealed class Key {
        object Main : Key()
        object Detail : Key()
    }

    lateinit var keys: BehaviorRelay<ICActiveMviKeys<Key>>
    lateinit var store: ICBasicMviFlowStore<Key>
    lateinit var mainScreenState: BehaviorRelay<String>
    lateinit var detailScreenState: BehaviorRelay<String>
    lateinit var subscriber: TestSubscriber<Option<ICMviState<Key, *>>>

    @Before
    fun setup() {
        keys = BehaviorRelay.createDefault(ICActiveMviKeys(emptyList()))
        mainScreenState = BehaviorRelay.createDefault("main-initial")
        detailScreenState = BehaviorRelay.createDefault("detail-initial")

        store = ICBasicMviFlowStore.init(keys.toFlowable()) {
            register(Key.Main::class, init = {
                mainScreenState.toFlowable()
            })

            register(Key.Detail::class, init = {
                detailScreenState.toFlowable()
            })
        }

        subscriber = TestSubscriber()
        store.screen().subscribe(subscriber)

        keys.accept(ICActiveMviKeys(listOf(Key.Main)))
        keys.accept(
            ICActiveMviKeys(
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

    private fun expectedInitialState(): List<Option<ICMviState<out Key, String>>> {
        return listOf(
            Option.empty(),
            Option.just(
                ICMviState(
                    Key.Main,
                    "main-initial"
                )
            ),
            Option.empty(),
            Option.just(
                ICMviState(
                    Key.Detail,
                    "detail-initial"
                )
            )

        )
    }
}
