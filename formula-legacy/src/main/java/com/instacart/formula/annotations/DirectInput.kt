package com.instacart.formula.annotations

/**
 * Often times we want to transform our state directly by a user input.
 * Marking a method inside a reducer factory with [DirectInput] will
 * generate the relay and all the bindings so you don't have to write
 * that boilerplate code yourself.
 *
 * Given:
 * ```
 * @ReducerFactory
 * class MyReducers {
 *   @DirectInput fun onButtonClick(input: ButtonClick): ReturnType {}
 * }
 * ```
 *
 * It will generate:
 * ```
 * class MyReducersEvents() {
 *   private val onButtonClick: BehaviorRelay<ButtonClick> = BehaviorRelay.create()
 *
 *   fun onButtonClick(action: ButtonClick): Unit {
 *     onButtonClick.accept(action)
 *   }
 *
 *   fun bind(actions: MyFeatureReducers): Observable<ReturnType> {
 *     val reducers = ArrayList<Observable<ReturnType>()
 *     reducers.add(onButtonClick.map { actions.onButtonClick(it) })
 *     return Observable.merge(reducers)
 *   }
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class DirectInput
