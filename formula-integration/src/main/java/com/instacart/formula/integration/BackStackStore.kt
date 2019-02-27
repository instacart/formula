package com.instacart.formula.integration

import com.instacart.formula.Reducer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

/**
 * A [BackStackStore] maintains back stack state and provides useful operations to modify it.
 *
 * This class is used internally to mimic fragment backstack. The activity listens for fragment
 * lifecycle events and update this store.
 *
 */
class BackStackStore<Key>(initial: List<Key>) {
    companion object {
        operator fun <Key> invoke(): BackStackStore<Key> {
            return BackStackStore(emptyList())
        }

        operator fun <Key> invoke(initial: Key): BackStackStore<Key> {
            return BackStackStore(listOf(initial))
        }
    }

    private val backStackStateRelay = BehaviorSubject.createDefault(BackStack(initial))

    fun stateChanges(): Flowable<BackStack<Key>> {
        return backStackStateRelay.toFlowable(BackpressureStrategy.BUFFER).distinctUntilChanged()
    }

    fun navigateBack() {
        updateState {
            val keys = it.keys
            if (keys.isEmpty()) {
                it
            } else {
                BackStack(keys.dropLast(1))
            }
        }
    }

    fun navigateTo(key: Key) {
        updateState {
            it.add(key)
        }
    }

    fun close(key: Key) {
        updateState {
            it.remove(key)
        }
    }

    fun onLifecycleEffect(lifecycleEvent: LifecycleEvent<Key>) {
        updateState {
            it.update(lifecycleEvent)
        }
    }

    private inline fun updateState(modify: Reducer<BackStack<Key>>) {
        backStackStateRelay.onNext(modify(backStackStateRelay.value!!))
    }
}
