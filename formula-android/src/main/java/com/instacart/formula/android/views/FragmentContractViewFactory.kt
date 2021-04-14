package com.instacart.formula.android.views

import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewFactory
import com.instacart.formula.android.ViewInstance
import com.instacart.formula.fragment.FragmentContract

/**
 * An implementation of [ViewFactory] which delegates implementation to [FragmentContract].
 */
internal class FragmentContractViewFactory<RenderModel>(
    private val contract: FragmentContract<RenderModel>
) : LayoutViewFactory<RenderModel>(layoutId = contract.layoutId) {

    override fun ViewInstance.create(): FeatureView<RenderModel> {
        val component = contract.createComponent(view)
        return featureView(
            renderView = component.renderView,
            lifecycleCallbacks = component.lifecycleCallbacks
        )
    }
}