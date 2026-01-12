package com.instacart.formula.android.fakes

import com.instacart.formula.IFormula
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteId
import com.instacart.formula.runAsStateFlow
import com.instacart.testutils.android.StateFlowKey
import com.instacart.testutils.android.TestViewFactory

class StateFlowFeatureFactory(
    private val formula: IFormula<RouteId<*>, Any>
) : FeatureFactory<Unit, StateFlowKey>() {
    override fun Params.initialize(): Feature {
        return Feature(
            renderFactory = TestViewFactory(),
            initAsync = key.initAsync,
        ) {
            formula.runAsStateFlow(it, routeId)
        }
    }
}
