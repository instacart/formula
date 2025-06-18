package com.instacart.formula.android

import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * A FragmentStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentStore @PublishedApi internal constructor(
    private val featureComponent: FeatureComponent<*>,
) {
    companion object {
        val EMPTY = init {  }

        fun init(
            init: FeaturesBuilder<Unit>.() -> Unit
        ): FragmentStore {
            return init(Unit, init)
        }

        fun <Component> init(
            rootComponent: Component,
            init: FeaturesBuilder<Component>.() -> Unit
        ): FragmentStore {
            val features = FeaturesBuilder.build(init)
            return init(rootComponent, features)
        }

        fun <Component> init(
            component: Component,
            features: Features<Component>
        ): FragmentStore {
            val featureComponent = FeatureComponent(component, features.bindings)
            return FragmentStore(featureComponent)
        }
    }

    @Volatile private var disposed = false
    private val dispatcher = MainThreadDispatcher()
    private val updateRelay = PublishRelay.create<Unit>()
    private var state = FragmentState()
    private var runningFeatures = mutableMapOf<FragmentId, Disposable>()

    private lateinit var fragmentEnvironment: FragmentEnvironment

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        val fragmentId = event.fragmentId
        when (event) {
            is FragmentLifecycleEvent.Added -> handleNewFragment(fragmentId)
            is FragmentLifecycleEvent.Removed -> handleRemoveFragment(fragmentId)
        }
    }

    internal fun onVisibilityChanged(fragmentId: FragmentId, visible: Boolean) {
        dispatcher.dispatch {
            val visibleIds = if (visible) {
                state.visibleIds.plus(fragmentId)
            } else {
                state.visibleIds.minus(fragmentId)
            }
            state = state.copy(visibleIds = visibleIds)
            updateRelay.accept(Unit)
        }
    }

    // TODO: should not be an observable.
    internal fun state(environment: FragmentEnvironment): Observable<FragmentState> {
        // TODO: should be set differently
        fragmentEnvironment = environment
        return updateRelay.startWithItem(Unit).map { state }.distinctUntilChanged()
    }

    fun dispose() {
        disposed = true

        dispatcher.dispatch {
            for (running in runningFeatures) {
                running.value.dispose()
            }

            runningFeatures.clear()
        }
    }

    private fun handleNewFragment(fragmentId: FragmentId) {
        dispatcher.dispatch {
            if (disposed) return@dispatch
            if (state.activeIds.contains(fragmentId)) return@dispatch

            val featureEvent = featureComponent.init(fragmentEnvironment, fragmentId)
            state = state.copy(
                activeIds = state.activeIds.plus(fragmentId),
                features = state.features.plus(featureEvent.id to featureEvent)
            )

            runFeature(fragmentEnvironment, featureEvent)

            updateRelay.accept(Unit)
        }
    }

    private fun handleRemoveFragment(fragmentId: FragmentId) {
        dispatcher.dispatch {
            if (disposed) return@dispatch

            runningFeatures.remove(fragmentId)?.dispose()

            if (state.activeIds.contains(fragmentId)) {
                state = state.copy(
                    activeIds = state.activeIds.minus(fragmentId),
                    features = state.features.minus(fragmentId),
                    outputs = state.outputs.minus(fragmentId),
                )
                updateRelay.accept(Unit)
            }
        }
    }

    private fun runFeature(environment: FragmentEnvironment, event: FeatureEvent) {
        val fragmentId = event.id
        val feature = (event as? FeatureEvent.Init)?.feature
        if (feature != null) {
            val observable = feature.stateObservable.onErrorResumeNext {
                environment.onScreenError(fragmentId.key, it)
                Observable.empty()
            }

            runningFeatures[fragmentId] = observable.subscribe {
                publishUpdate(fragmentId, it)
            }
        }
    }

    private fun publishUpdate(fragmentId: FragmentId, output: Any) {
        dispatcher.dispatch {
            if (!state.activeIds.contains(fragmentId)) {
                return@dispatch
            }

            val keyState = FragmentOutput(fragmentId.key, output)
            state = state.copy(outputs = state.outputs.plus(fragmentId to keyState))

            updateRelay.accept(Unit)
        }
    }
}
