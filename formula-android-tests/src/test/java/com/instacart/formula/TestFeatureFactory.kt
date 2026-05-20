package com.instacart.formula

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey
import com.instacart.testutils.android.TestViewFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class TestFeatureFactory<Key : RouteKey>(
    private val render: (RouteKey, Any) -> Unit = { _, _ -> },
    private val state: (Key) -> (CoroutineScope) -> StateFlow<Any>,
) : FeatureFactory<Unit, Key>() {
    override fun Params.initialize(): Feature {
        return Feature(
            viewFactory = TestViewFactory { value ->
                render(key, value)
            }
        ) {
            state(key)(it)
        }
    }
}
