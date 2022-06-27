package com.instacart.formula.android

import com.instacart.formula.android.fakes.NoOpViewFactory
import io.reactivex.rxjava3.core.Observable

object TestUtils {
    fun <Value : Any> feature(stateValue: Value): Feature<Value> {
        return Feature(
            state = Observable.just(stateValue),
            viewFactory = NoOpViewFactory()
        )
    }

    fun <Dependencies, Key : FragmentKey> featureFactory(
        init: (Dependencies, Key) -> Observable<Any>
    ): FeatureFactory<Dependencies, Key> {
        return object : FeatureFactory<Dependencies, Key> {
            override fun initialize(dependencies: Dependencies, key: Key): Feature<*> {
                return Feature(
                    state = init(dependencies, key),
                    viewFactory = NoOpViewFactory()
                )
            }
        }
    }
}