package com.instacart.formula.android

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.instacart.formula.android.views.DelegateLayoutViewFactory

/**
 * View factory is used by [FormulaFragment] to create a [FeatureView] which contains
 * the root Android view and the logic to bind the state management observable to this
 * view.
 *
 * To create a view factory, use static constructor [fromLayout] or extend [LayoutViewFactory].
 */
fun interface ViewFactory<RenderModel> {

    companion object {
        /**
         * Creates a [ViewFactory] which uses [layoutId] to inflate an Android view.
         *
         * ```
         * val viewFactory = ViewFactory.fromLayout(R.layout.task_screen) {
         *
         *   // We have access the inflated view directly by calling [ViewInstance.view]
         *   val taskNameTextView = view.findViewById(R.id.task_name_text_view)
         *
         *   // Create [Renderer] or [RenderView]
         *   val renderer = Renderer { model: TaskRenderModel ->
         *     taskNameTextView.text = model.counterText
         *   }
         *
         *   // Finish feature view creation
         *   featureView(renderer = renderer)
         * }
         * ```
         *
         * @param layoutId Layout resource to be inflated.
         * @param createView Called with a [ViewInstance] to finish [FeatureView] creation
         */
        fun <RenderModel> fromLayout(
            @LayoutRes layoutId: Int,
            createView: ViewInstance.() -> FeatureView<RenderModel>
        ): ViewFactory<RenderModel> {
            return DelegateLayoutViewFactory(layoutId, createView)
        }
    }

    /**
     * This method is called from [FormulaFragment.onCreateView] function. Use it to
     * instantiate an Android view instance and return a [FeatureView] which knows how to
     * bind the state management to view rendering. Usually, you should use [LayoutViewFactory]
     * or [ViewFactory.fromLayout] instead of implementing this method directly.
     */
    fun create(inflater: LayoutInflater, container: ViewGroup?): FeatureView<RenderModel>
}