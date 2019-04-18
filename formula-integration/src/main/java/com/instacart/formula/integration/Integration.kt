package com.instacart.formula.integration

import io.reactivex.Flowable

/**
 * Defines an integration for a specific [Key].
 *
 * @param Component a component that helps construct the state management stream.
 * @param Key a backstack entry for this screen.
 * @param RenderModel a render model that the state management produces.
 *
 * ```
 * class TaskListIntegration<AppComponent, TaskListKey, TaskListRenderModel>() {
 *   override fun create(component: AppComponent, key: TaskListKey): Flowable<TaskListRenderModel> {
 *     return component.createTaskListFormula().state(Input())
 *   }
 * }
 * ```
 */
abstract class Integration<in Component, in Key, RenderModel : Any> {

    abstract fun create(component: Component, key: Key): Flowable<RenderModel>
}
