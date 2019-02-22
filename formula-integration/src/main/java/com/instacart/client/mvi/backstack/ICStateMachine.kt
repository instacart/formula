package com.instacart.client.mvi.backstack

import com.instacart.client.core.func.Reducer
import com.instacart.client.mvi.ICActiveMviKeys
import com.instacart.client.mvi.ICMviLifecycleEvent
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
class ICStateMachine<Key>(initial: List<Key>) {
    companion object {
        operator fun <Key> invoke(): ICStateMachine<Key> {
            return ICStateMachine(emptyList())
        }

        operator fun <Key> invoke(initial: Key): ICStateMachine<Key> {
            return ICStateMachine(listOf(initial))
        }
    }

    private val activeKeysRelay = BehaviorSubject.createDefault(ICActiveMviKeys(initial))

    fun stateChanges(): Flowable<ICActiveMviKeys<Key>> {
        return activeKeysRelay.toFlowable(BackpressureStrategy.BUFFER).distinctUntilChanged()
    }

    fun navigateBack() {
        updateState {
            val keys = it.activeKeys
            if (keys.isEmpty()) {
                it
            } else {
                ICActiveMviKeys(keys.dropLast(1))
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

    fun onLifecycleEffect(lifecycleEvent: ICMviLifecycleEvent<Key>) {
        updateState {
            it.update(lifecycleEvent)
        }
    }

    private inline fun updateState(modify: Reducer<ICActiveMviKeys<Key>>) {
        activeKeysRelay.onNext(modify(activeKeysRelay.value!!))
    }
}
