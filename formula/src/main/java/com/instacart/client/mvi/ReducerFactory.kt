package com.instacart.client.mvi

/**
 * This annotation will scan all public methods
 * with a single action parameter and generate
 * an events class.
 *
 * Requirements:
 * All methods need to take a single parameter and return the same type.
 *
 * Limitations:
 * 1. Kotlin nullability declaration doesn't work. As a workaround, use Option type.
 *
 * Given:
 *
 * ```
 * @ReducerFactory
 * class MyFeatureReducers {
 *   fun onUserEvent(event: UserEvent): ReturnType {}
 *   fun onDataEvent(event: DataEvent): ReturnType {}
 *   fun onAnotherEvent(event: AnotherEvent): ReturnType {}
 * }
 *```
 *
 * It will generate:
 *```
 * class MyFeatureReducersEvents(
 *   val onUserEvent: Flowable<UserEvent>,
 *   val onDataEvent: Flowable<DataEvent>,
 *   val onAnotherEvent: Flowable<AnotherEvent>
 * ) {
 *  fun bind(actions: MyFeatureReducers): Flowable<ReturnType> {
 *    val reducers = ArrayList<Flowable<ReturnType>()
 *    reducers.add(onUserEvent.map { actions.onUserEvent(it) })
 *    reducers.add(onDataEvent.map { actions.onDataEvent(it) })
 *    reducers.add(onAnotherEvent.map { actions.onAnotherEvent(it) })
 *    return Flowable.merge(reducers)
 *  }
 * }
 * ```
 */
annotation class ReducerFactory
