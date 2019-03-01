package com.instacart.formula.annotations

import com.instacart.formula.Reducers
import kotlin.reflect.KClass

/**
 * Annotates a state data class for which a fully formed events class is generated from
 * [reducers] class and properties marked with [ExportedProperty].
 *
 * [reducers] - (Optional) A class that extends [com.instacart.formula.Reducers]
 * and contains various state transformations. The class will be scanned for functions that
 * have a single input parameter and return a [com.instacart.formula.NextReducer].
 * A binding will be generated for each of these methods.
 *
 * Requirements:
 * All methods need to take a single parameter and return (State) -> Next<State, Effect>
 *
 * Limitations:
 * 1. Kotlin nullability declaration doesn't work. As a workaround, use Option type.
 *
 *
 * Given:
 *
 * ```
 * @State(reducers = MyFeatureReducers::class)
 * data class MyState(
 *   @ExportedProperty val propertyA: String,
 *   val propertyB: String
 * )
 *
 * class MyFeatureReducers {
 *   fun onUserEvent(event: UserEvent): (MyState) -> Next<MyState, Effect> {}
 *   fun onDataEvent(event: DataEvent): (MyState) -> Next<MyState, Effect> {}
 *   fun onAnotherEvent(event: AnotherEvent): (MyState) -> Next<MyState, Effect> {}
 * }
 *```
 *
 * It will generate:
 *```
 * class ICMyFeatureGeneratedReducers<Effect> {
 *   fun onPropertyAChanged(action: String): (MyState) -> Next<MyState, Effect> = { state: MyState ->
 *      Next(state.copy(propertyA = action), Collections.emptySet())
 *   }
 * }
 *
 * class MyFeatureStateEvents(private val reducers: MyFeatureReducers) {
 *
 *  private val generatedReducers = ICMyFeatureGeneratedReducers<Effect>()
 *
 *  fun bind(
 *    onPropertyAChanged: Flowable<String>,
 *    onUserEvent: Flowable<UserEvent>,
 *    onDataEvent: Flowable<DataEvent>,
 *    onAnotherEvent: Flowable<AnotherEvent>
 *  ): Flowable<(MyState) -> Next<MyState, Effect>> {
 *    val reducers = ArrayList<Flowable<ReturnType>()
 *    reducers.add(onPropertyAChanged.map { generatedReducers.onPropertyAChanged(it) })
 *    reducers.add(onUserEvent.map { reducers.onUserEvent(it) })
 *    reducers.add(onDataEvent.map { reducers.onDataEvent(it) })
 *    reducers.add(onAnotherEvent.map { reducers.onAnotherEvent(it) })
 *    return Flowable.merge(reducers)
 *  }
 * }
 * ```
 *
 */
annotation class State(
    /**
     * (Optional) A class that extends [com.instacart.formula.Reducers]
     * and contains various state transformations. The class will be scanned for functions that
     * have a single input parameter and return a [com.instacart.formula.NextReducer].
     * A binding will be generated for each of these methods.
     */
    val reducers: KClass<out Reducers<*, *>> = Reducers::class
)
