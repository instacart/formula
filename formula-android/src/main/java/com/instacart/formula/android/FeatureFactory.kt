package com.instacart.formula.android

/**
 * Feature factory is responsible for creating a [Feature] for a specific [route key][Key].
 *
 * ```
 * // An example from samples/todoapp
 * class TaskListFeatureFactory : FeatureFactory<TaskListFeatureFactory.Dependencies, TaskListKey>() {
 *
 *   interface Dependencies {
 *     fun taskRepo(): TaskRepo
 *     fun taskListInput(): TaskListFormula.Input
 *   }
 *
 *   override fun initialize(dependencies: Dependencies, key: TaskListKey): Feature {
 *     // Note: we could create our own internal dagger component here using the dependencies.
 *     val formula = TaskListFormula(dependencies.taskRepo())
 *     return Feature(
 *       state = formula.toObservable(dependencies.taskListInput()),
 *       renderFactory = ViewFactory.fromLayout(R.layout.task_list) { view ->
 *         val renderView = TaskListRenderView(view)
 *         featureView(renderView)
 *       }
 *     )
 *   }
 * }
 * ```
 *
 * Once we define a [FeatureFactory], we need to [bind][FeaturesBuilder.bind] it to a
 * [NavigationStore]. The navigation flow store will call [initialize] the first time
 * [FormulaFragment] with a new [Key] is attached. It will subscribe to the state management
 * and persist it across configuration changes.
 *
 * @param Dependencies dependencies to instantiate this feature. Usually defined by an interface.
 * @param Key a type of route key that is used to identify this feature.
 *
 */
abstract class FeatureFactory<in Dependencies, in Key : RouteKey> {

    inner class Params(
        val dependencies: @UnsafeVariance Dependencies,
        val routeId: RouteId<@UnsafeVariance Key>,
    ) {
        val key = routeId.key
    }

    /**
     * Initializes the [Feature] using [Params] provided.
     */
    abstract fun Params.initialize(): Feature

    /**
     * Initializes state observable and a view factory for a specific [key].
     */
    fun initialize(dependencies: Dependencies, routeId: RouteId<@UnsafeVariance Key>): Feature {
        return Params(dependencies, routeId).initialize()
    }
}
