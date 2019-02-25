package com.instacart.formula.integration

import com.instacart.formula.Reducer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

/**
 * Used to manage app backstack.
 *
 * Ex: activity would call your view model to set active mvi contracts and
 * your view model would delegate to this class.
 *
 */
class BackstackStore<Key>(initial: List<Key>) {
    companion object {
        operator fun <Key> invoke(): BackstackStore<Key> {
            return BackstackStore(emptyList())
        }

        operator fun <Key> invoke(initial: Key): BackstackStore<Key> {
            return BackstackStore(listOf(initial))
        }
    }

    private val activeKeysRelay = BehaviorSubject.createDefault(ActiveKeys(initial))

    fun stateChanges(): Flowable<ActiveKeys<Key>> {
        return activeKeysRelay.toFlowable(BackpressureStrategy.BUFFER).distinctUntilChanged()
    }

    fun navigateBack() {
        updateState {
            val keys = it.activeKeys
            if (keys.isEmpty()) {
                it
            } else {
                ActiveKeys(keys.dropLast(1))
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

    private inline fun updateState(modify: Reducer<ActiveKeys<Key>>) {
        activeKeysRelay.onNext(modify(activeKeysRelay.value!!))
    }
}
