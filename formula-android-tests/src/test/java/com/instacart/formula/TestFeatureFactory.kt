package com.instacart.formula

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable

class TestFeatureFactory<Key : RouteKey>(
    private val render: (RouteKey, Any) -> Unit = { _, _ -> },
    private val state: (Key) -> Observable<Any>,
) : FeatureFactory<Unit, Key>() {
    override fun Params.initialize(): Feature {
        return Feature(
            state = state(key),
            renderFactory = TestViewFactory { _, value ->
                render(key, value)
            }
        )
    }
}