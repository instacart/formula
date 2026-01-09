package com.instacart.formula.android

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.instacart.formula.android.views.DelegateLayoutViewFactory

/**
 * View factory is used by [FormulaFragment] to create a [ViewFeatureView] which contains
 * the root Android view and the logic to bind the state management observable to this
 * view.
 *
 * To create a view factory, use static constructor [fromLayout] or extend [LayoutViewFactory].
 *
 * @see RenderFactory for the base interface
 * @see com.instacart.formula.android.compose.ComposeRenderFactory for Nav3 Compose rendering (in formula-android-compose)
 */
fun interface ViewFactory<RenderModel> : RenderFactory<RenderModel> {

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
         * @param createView Called with a [ViewInstance] to finish [ViewFeatureView] creation
         */
        fun <RenderModel> fromLayout(
            @LayoutRes layoutId: Int,
            createView: ViewInstance.() -> ViewFeatureView<RenderModel>
        ): ViewFactory<RenderModel> {
            return DelegateLayoutViewFactory(layoutId, createView)
        }
    }

    class Params(
        val context: Context,
        val inflater: LayoutInflater,
        val container: ViewGroup?,
    )

    /**
     * This method is called from [FormulaFragment.onCreateView] function. Use it to
     * instantiate an Android view instance and return a [ViewFeatureView] which knows how to
     * bind the state management to view rendering. Usually, you should use [LayoutViewFactory]
     * or [ViewFactory.fromLayout] instead of implementing this method directly.
     */
    fun create(params: Params): ViewFeatureView<RenderModel>
}