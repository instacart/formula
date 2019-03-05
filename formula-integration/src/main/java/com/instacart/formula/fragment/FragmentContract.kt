package com.instacart.formula.fragment

import android.os.Parcelable
import android.view.View

/**
 * Responsible for providing a [FragmentComponent]
 * that can take a [RenderModel] object and apply rendering to the view.
 * Ex.
 * ```
 * @Parcelize
 * data class TaskListContract(
 *  override val tag: String = "task list",
 *  override val layoutId: Int = R.layout.task_list
 * ) : FragmentContract<TaskListRenderModel>() {
 *
 *  override fun createComponent(view: View): FragmentComponent<TaskListRenderModel> {
 *     val renderView = TaskListRenderView(view)
 *     return FragmentComponent.create(renderView)
 *  }
 * }
 * ```
 */
abstract class FragmentContract<in RenderModel> : Parcelable {

    /**
     * Tag that will be used to determine fragment visibility. This tag should be unique for each contract, though this
     * is not enforced.
     */
    abstract val tag: String

    /**
     * Layout id that defines the view
     */
    abstract val layoutId: Int

    /**
     * Takes an Android view and creates a [FragmentComponent]
     */
    abstract fun createComponent(view: View): FragmentComponent<RenderModel>
}
