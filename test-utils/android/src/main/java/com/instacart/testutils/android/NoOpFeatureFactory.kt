package com.instacart.testutils.android

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey
import com.instacart.formula.android.ViewFactory
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

class NoOpFeatureFactory<FragmentKeyT : RouteKey>(
    private val viewFactory: ViewFactory<FragmentKeyT> = TestViewFactory(),
) : FeatureFactory<Unit, FragmentKeyT>() {
    @Suppress("UNCHECKED_CAST")
    override fun Params.initialize(): Feature {
        return Feature(
            viewFactory = viewFactory,
        ) {
            NeverEmitStateFlow as StateFlow<FragmentKeyT>
        }
    }
}

private object NeverEmitStateFlow : StateFlow<Any> {
    override val value: Any get() = Unit
    override val replayCache: List<Any> = emptyList()
    override suspend fun collect(collector: FlowCollector<Any>): Nothing {
        awaitCancellation()
    }
}
