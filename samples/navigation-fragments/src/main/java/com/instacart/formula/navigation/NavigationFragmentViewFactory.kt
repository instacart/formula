package com.instacart.formula.navigation

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory

class NavigationFragmentViewFactory : ViewFactory<NavigationFragmentRenderModel> {

    override fun create(params: ViewFactory.Params): FeatureView<NavigationFragmentRenderModel> {
        val view = createView(params)

        return FeatureView(
            view = view,
            setOutput = { renderModel ->
                bindView(view, renderModel)
            },
            lifecycleCallbacks = null
        )
    }

    private fun createView(params: ViewFactory.Params): View {
        val context = params.context
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Fragment title
        val titleText = TextView(context).apply {
            id = R.id.fragment_title
            textSize = 24f
        }
        rootLayout.addView(titleText)

        // Counter text
        val counterText = TextView(context).apply {
            id = R.id.counter_text
            textSize = 18f
        }
        rootLayout.addView(counterText)

        // Navigate next button
        val navigateNextButton = Button(context).apply {
            id = R.id.navigate_next_button
            text = "Navigate to Next Fragment"
        }
        rootLayout.addView(navigateNextButton)

        // Navigate back button
        val navigateBackButton = Button(context).apply {
            id = R.id.navigate_back_button
            text = "Navigate Back"
        }
        rootLayout.addView(navigateBackButton)

        // Container for back stack buttons
        val backStackContainer = LinearLayout(context).apply {
            id = R.id.back_stack_container
            orientation = LinearLayout.VERTICAL
        }
        rootLayout.addView(backStackContainer)

        return rootLayout
    }

    private fun bindView(view: View, renderModel: NavigationFragmentRenderModel) {
        val titleText = view.findViewById<TextView>(R.id.fragment_title)
        val counterText = view.findViewById<TextView>(R.id.counter_text)
        val navigateNextButton = view.findViewById<Button>(R.id.navigate_next_button)
        val navigateBackButton = view.findViewById<Button>(R.id.navigate_back_button)
        val backStackContainer = view.findViewById<LinearLayout>(R.id.back_stack_container)

        titleText.text = "Fragment ${renderModel.fragmentId}"
        counterText.text = "Counter: ${renderModel.counter}"

        navigateNextButton.setOnClickListener { renderModel.onNavigateToNext() }
        navigateBackButton.setOnClickListener { renderModel.onNavigateBack() }

        // Show/hide back button based on back stack
        navigateBackButton.visibility = if (renderModel.backStackFragments.isNotEmpty()) View.VISIBLE else View.GONE

        // Clear and rebuild back stack buttons
        backStackContainer.removeAllViews()
        if (renderModel.backStackFragments.isNotEmpty()) {
            val backStackLabel = TextView(view.context).apply {
                text = "Back Stack Fragments:"
                textSize = 16f
            }
            backStackContainer.addView(backStackLabel)

            renderModel.backStackFragments.forEach { fragmentId ->
                val button = Button(view.context).apply {
                    text = "Increment Counter for Fragment $fragmentId"
                    setOnClickListener { renderModel.onIncrementCounter(fragmentId) }
                }
                backStackContainer.addView(button)
            }
        }
    }
}