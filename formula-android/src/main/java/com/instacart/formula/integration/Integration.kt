package com.instacart.formula.integration

import io.reactivex.rxjava3.core.Observable

/**
 * Defines an integration for a specific [Key].
 *
 * @param Component a component that helps construct the state management stream.
 * @param Key a backstack entry for this screen.
 * @param RenderModel a render model that the state management produces.
 *
 * ```
 * class TaskListIntegration<AppComponent, TaskListKey, TaskListRenderModel>() {
 *   override fun create(component: AppComponent, key: TaskListKey): Observable<TaskListRenderModel> {
 *     return component.createTaskListFormula().toObservable(Input())
 *   }
 * }
 * ```
 */
abstract class Integration<in Component, in Key, RenderModel : Any> {

    abstract fun create(component: Component, key: Key): Observable<RenderModel>
}
