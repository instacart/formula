package com.instacart.formula

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable

class TestFeatureFactory<Key : FragmentKey>(
    private val render: (FragmentKey, Any) -> Unit = { _, _ -> },
    private val state: (Key) -> Observable<Any>,
) : FeatureFactory<Unit, Key>() {
    override fun Params.initialize(): Feature {
        return Feature(
            state = state(key),
            viewFactory = TestViewFactory { _, value ->
                render(key, value)
            }
        )
    }
}