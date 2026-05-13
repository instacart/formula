package com.instacart.formula.android

import com.instacart.testutils.android.TestViewFactory
import kotlinx.coroutines.flow.MutableStateFlow

object TestUtils {
    fun <Value : Any> feature(stateValue: Value): Feature {
        return Feature(
            viewFactory = TestViewFactory()
        ) {
            MutableStateFlow(stateValue)
        }
    }
}
