package com.instacart.formula.fragment

import android.view.View
import com.instacart.formula.integration.FragmentId
import com.instacart.formula.integration.KeyState
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
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

        fragmentStoreStates()
            .apply {
                store.onLifecycleEffect(master.asAddedEvent())
                store.onLifecycleEffect(detail.asAddedEvent())

                updateRelay.accept(master to "master-update")
                store.onLifecycleEffect(detail.asRemovedEvent())
                store.onLifecycleEffect(master.asRemovedEvent())

                updateRelay.accept(master to "master-update-2")
            }
            .assertValues(
                expectedState(),
                expectedState(master to "master-1-state"),
                expectedState(master to "master-1-state", detail to "detail-1-state"),
                expectedState(master to "master-update", detail to "detail-1-state"),
                expectedState(master to "master-update"),
                expectedState()
            )
    }

    @Test fun `various fragments added`() {
        fragmentStoreStates()
            .apply {
                store.onLifecycleEffect(Master(1).asAddedEvent())
                store.onLifecycleEffect(Detail(1).asAddedEvent())
                store.onLifecycleEffect(Detail(2).asAddedEvent())
            }
            .assertValues(
                expectedState(),
                expectedState(Master(1) to "master-1-state"),
                expectedState(Master(1) to "master-1-state", Detail(1) to "detail-1-state"),
                expectedState(
                    Master(1) to "master-1-state",
                    Detail(1) to "detail-1-state",
                    Detail(2) to "detail-2-state"
                )
            )
    }

    private fun fragmentStoreStates(): TestObserver<Map<FragmentKey, KeyState>> {
        return store.state(FragmentEnvironment())
            .map { it.states.mapKeys { entry -> entry.key.key } }
            .test()
    }

    private fun expectedState(vararg states: Pair<FragmentContract<*>, *>): Map<FragmentKey, KeyState> {
        return expectedState(states.asList())
    }

    private fun expectedState(states: List<Pair<FragmentContract<*>, *>>): Map<FragmentKey, KeyState> {
        val initial = mutableMapOf<FragmentKey, KeyState>()
        return states.foldRight(initial) { value, acc ->
            if (value.second != null) {
                acc.put(value.first, KeyState(value.first, value.second!!))
            }

            acc
        }
    }

    private fun state(key: FragmentContract<*>): Observable<String> {
        val updates = updateRelay.filter { it.first == key }.map { it.second }
        return updates.startWithItem("${key.tag}-state")
    }

    private fun FragmentKey.asAddedEvent() = FragmentLifecycleEvent.Added(FragmentId("", this))
    private fun FragmentKey.asRemovedEvent() = FragmentLifecycleEvent.Removed(FragmentId("", this))
}
