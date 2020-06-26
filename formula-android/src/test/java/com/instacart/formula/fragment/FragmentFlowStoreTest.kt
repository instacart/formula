package com.instacart.formula.fragment

import android.view.View
import com.instacart.formula.integration.KeyState
import com.instacart.formula.integration.LifecycleEvent
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.parcel.Parcelize
import org.junit.Before
import org.junit.Test

class FragmentFlowStoreTest {

    @Parcelize
    data class Master(
        val id: Int,
        override val tag: String = "master-$id",
        override val layoutId: Int = -1
    ) : FragmentContract<String>() {
        override fun createComponent(view: View): FragmentComponent<String> {
            return FragmentComponent.noOp()
        }
    }

    @Parcelize
    data class Detail(
        val id: Int,
        override val tag: String = "detail-$id",
        override val layoutId: Int = -1
    ) : FragmentContract<String>() {
        override fun createComponent(view: View): FragmentComponent<String> {
            return FragmentComponent.noOp()
        }
    }

    lateinit var store: FragmentFlowStore
    lateinit var updateRelay: PublishRelay<Pair<FragmentContract<*>, String>>

    @Before fun setup() {
        updateRelay = PublishRelay.create()

        FragmentEnvironment()
        store = FragmentFlowStore.init {
            bind(Master::class) { key ->
                state(key)
            }
            bind(Detail::class) { key ->
                state(key)
            }
        }
    }

    @Test fun `subscribed to state until removed from backstack`() {
        val master = Master(1)
        val detail = Detail(1)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(LifecycleEvent.Added(master))
                store.onLifecycleEffect(LifecycleEvent.Added(detail))

                updateRelay.accept(master to "master-update")
                store.onLifecycleEffect(LifecycleEvent.Removed(detail))
                store.onLifecycleEffect(LifecycleEvent.Removed(master))

                updateRelay.accept(master to "master-update-2")
            }
            .assertValues(
                expectedState(),
                expectedState(master to null),
                expectedState(master to "master-1-state"),
                expectedState(master to "master-1-state", detail to null),
                expectedState(master to "master-1-state", detail to "detail-1-state"),
                expectedState(master to "master-update", detail to "detail-1-state"),
                expectedState(master to "master-update"),
                expectedState()
            )
    }

    @Test fun `various fragments added`() {
        store.state(FragmentEnvironment()).test()
            .apply {
                store.onLifecycleEffect(LifecycleEvent.Added(Master(1)))
                store.onLifecycleEffect(LifecycleEvent.Added(Detail(1)))
                store.onLifecycleEffect(LifecycleEvent.Added(Detail(2)))
            }
            .assertValues(
                expectedState(),
                expectedState(Master(1) to null),
                expectedState(Master(1) to "master-1-state"),
                expectedState(Master(1) to "master-1-state", Detail(1) to null),
                expectedState(Master(1) to "master-1-state", Detail(1) to "detail-1-state"),
                expectedState(Master(1) to "master-1-state", Detail(1) to "detail-1-state", Detail(2) to null),
                expectedState(
                    Master(1) to "master-1-state",
                    Detail(1) to "detail-1-state",
                    Detail(2) to "detail-2-state"
                )
            )
    }

    private fun expectedState(vararg states: Pair<FragmentContract<*>, *>): FragmentFlowState {
        return expectedState(states.asList())
    }

    private fun expectedState(states: List<Pair<FragmentContract<*>, *>>): FragmentFlowState {
        val initial = mutableMapOf<FragmentContract<*>, KeyState<FragmentContract<*>>>()
        val keyStates = states.foldRight(initial) { value, acc ->
            if (value.second != null) {
                acc.put(value.first, KeyState(value.first, value.second!!))
            }

            acc
        }

        return FragmentFlowState(
            activeKeys = states.map { it.first },
            states = keyStates
        )
    }

    private fun state(key: FragmentContract<*>): Observable<String> {
        val updates = updateRelay.filter { it.first == key }.map { it.second }
        return updates.startWithItem("${key.tag}-state")
    }
}
