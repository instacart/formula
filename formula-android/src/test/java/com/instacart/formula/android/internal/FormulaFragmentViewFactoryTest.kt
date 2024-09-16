package com.instacart.formula.android.internal

import com.google.common.truth.Truth
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.ViewFactory
import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import java.lang.RuntimeException

class FormulaFragmentViewFactoryTest {

    @Test fun `throws an exception if feature provider returns null`() {
        val viewFactory = viewFactory { null }
        val result = runCatching { viewFactory.viewFactory() }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Could not find feature for "
        )
    }

    @Test fun `throws an exception if feature is not registered`() {
        val viewFactory = viewFactory {
            FeatureEvent.MissingBinding(it)
        }
        val result = runCatching { viewFactory.viewFactory() }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Missing feature factory or integration for"
        )
    }

    @Test fun `throws an exception if feature failed to initialize`() {
        val viewFactory = viewFactory {
            FeatureEvent.Failure(it, RuntimeException("Something went wrong"))
        }
        val result = runCatching { viewFactory.viewFactory() }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Feature failed to initialize:"
        )
    }

    @Test fun `only initializes the view factory once`() {
        var timesCalled = 0
        val viewFactory = viewFactory {
            timesCalled += 1
            val feature = Feature(
                state = Observable.empty(),
                viewFactory = ViewFactory { _, _ ->
                    error("should not be called")
                }
            )
            FeatureEvent.Init(it, feature)
        }

        viewFactory.viewFactory()
        viewFactory.viewFactory()
        viewFactory.viewFactory()
        viewFactory.viewFactory()
        Truth.assertThat(timesCalled).isEqualTo(1)
    }

    private fun viewFactory(
        delegateGetFeature: (FragmentId) -> FeatureEvent?,
    ): FormulaFragmentViewFactory {
        return FormulaFragmentViewFactory(
            environment = FragmentEnvironment(),
            fragmentId = FragmentId(
                instanceId = "instanceId",
                key = EmptyFragmentKey(tag = "tag")
            ),
            featureProvider = object : FeatureProvider {
                override fun getFeature(id: FragmentId): FeatureEvent? {
                    return delegateGetFeature(id)
                }
            }
        )
    }
}