package com.instacart.formula.android

import androidx.annotation.LayoutRes
import com.instacart.formula.android.views.InflatedViewInstance

/**
 * View factory which uses [layoutId] to inflate an Android view and then passes that view instance
 * to [create] to instantiate [FeatureView].
 *
 * ```
 * class TaskViewFactory : LayoutViewFactory<TaskRenderModel>(R.layout.task_screen) {
 *
 *   override ViewInstance.create(): FeatureView<TaskRenderModel> {
 *
 *     // We have access the inflated view directly by calling [ViewInstance.view]
 *     val taskNameTextView = view.findViewById(R.id.task_name_text_view)
 *
 *     // Create [Renderer] or [RenderView]
 *     val renderer = Renderer { model: TaskRenderModel ->
 *       taskNameTextView.text = model.counterText
 *     }
 *
 *     // Finish feature view creation
 *     return featureView(renderer = renderer)
 *   }
 * }
 * ```
 */
abstract class LayoutViewFactory<RenderModel>(@LayoutRes private val layoutId: Int): ViewFactory<RenderModel> {

    abstract fun ViewInstance.create(): FeatureView<RenderModel>

    override fun create(params: ViewFactory.Params): FeatureView<RenderModel> {
        val view = params.inflater.inflate(layoutId, params.container, false)
        return InflatedViewInstance(view).create()
    }
}