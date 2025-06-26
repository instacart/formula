package com.instacart.formula.android

import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

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

    private lateinit var environment: FragmentEnvironment
    private val features = mutableMapOf<FragmentId, FeatureEvent>()
    private val disposables = mutableMapOf<FragmentId, Disposable>()
    private val stateFlow = MutableStateFlow(FragmentState())

    internal fun onLifecycleEffect(environment: FragmentEnvironment, event: FragmentLifecycleEvent) {
        val fragmentId = event.fragmentId
        when (event) {
            is FragmentLifecycleEvent.Added -> {
                if (!features.contains(fragmentId)) {
                    val featureEvent = featureComponent.init(environment, fragmentId)
                    features[fragmentId] = featureEvent

                    stateFlow.update { state ->
                        if (!state.activeIds.contains(featureEvent.id)) {
                            state.copy(
                                activeIds = state.activeIds.plus(featureEvent.id),
                            )
                        } else {
                            state
                        }
                    }

                    if (featureEvent is FeatureEvent.Init) {
                        val observable = featureEvent.feature.stateObservable.onErrorResumeNext {
                            environment.onScreenError(fragmentId.key, it)
                            Observable.empty()
                        }

                        val disposable = observable.subscribe { update ->
                            stateFlow.update { state ->
                                if (state.activeIds.contains(fragmentId)) {
                                    val keyState = FragmentOutput(fragmentId.key, update)
                                    state.copy(outputs = state.outputs.plus(fragmentId to keyState))
                                } else {
                                    state
                                }
                            }
                        }
                        disposables[fragmentId] = disposable
                    }
                }
            }
            is FragmentLifecycleEvent.Removed -> {
                features.remove(fragmentId)

                // Cancel running feature job.
                disposables.remove(fragmentId)?.dispose()

                stateFlow.update { state ->
                    state.copy(
                        activeIds = state.activeIds.minus(fragmentId),
                        outputs = state.outputs.minus(fragmentId),
                    )
                }
            }
        }
    }

    internal fun onVisibilityChanged(fragmentId: FragmentId, visible: Boolean) {
        if (visible) {
            stateFlow.update { state ->
                if (state.visibleIds.contains(fragmentId)) {
                    state
                } else {
                    state.copy(visibleIds = state.visibleIds.plus(fragmentId))
                }
            }
        } else {
            stateFlow.update { state ->
                state.copy(visibleIds = state.visibleIds.minus(fragmentId))
            }
        }
    }

    internal fun state(): Observable<FragmentState> {
        return Observable.create { emitter ->
            val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
                withContext(Dispatchers.Main.immediate) {
                    stateFlow.collect { emitter.onNext(it) }
                }
            }

            emitter.setCancellable { job.cancel() }
        }
    }

    fun dispose() {
        for (entry in disposables) {
            entry.value.dispose()
        }
        disposables.clear()
    }

    internal fun getViewFactory(fragmentId: FragmentId): ViewFactory<Any>? {
        return try {
            findViewFactoryOrThrow(fragmentId)
        } catch (e: Throwable) {
            environment.onScreenError(fragmentId.key, e)
            null
        }
    }

    private fun findViewFactoryOrThrow(fragmentId: FragmentId): ViewFactory<Any> {
        val key = fragmentId.key
        val featureEvent = features[fragmentId] ?: throw IllegalStateException("Could not find feature for $key.")
        return when (featureEvent) {
            is FeatureEvent.MissingBinding -> {
                throw IllegalStateException("Missing feature factory or integration for $key. Please check your FragmentStore configuration.")
            }
            is FeatureEvent.Failure -> {
                throw IllegalStateException("Feature failed to initialize: $key", featureEvent.error)
            }
            is FeatureEvent.Init -> {
                featureEvent.feature.viewFactory
            }
        }
    }
}
