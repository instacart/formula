package com.instacart.formula

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.test.TestFragmentActivity
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable

class TestFeatureFactory<Key : FragmentKey>(
    private val applyOutput: (Any) -> Unit = {},
    private val state: (Key) -> Observable<Any>,
) : FeatureFactory<Unit, Key> {
    override fun initialize(dependencies: Unit, key: Key): Feature {
        return Feature(
            state = state(key),
            viewFactory = TestViewFactory { view, value ->
                (view.context as TestFragmentActivity).renderCalls.add(Pair(key, value))
                applyOutput(value)
            }
        )
    }
}