package com.instacart.formula.integration

/**
 * Defines a component that can be disposed. This enables us to clean up
 * once the component is not needed anymore.
 *
 * ```
 * // An example of how to create a disposable component.
 * fun createComponent(): DisposableScope<Component> {
 *   val component = Component()
 *   component.service.start()
 *   return DisposableScope(component, onDispose = {
 *     component.service.stop()
 *   })
 * }
 *
 * val component = createComponent()
 *
 * .. do something with the component
 *
 * // clean up
 * component.dispose()
 * ```
 */
class DisposableScope<out Component>(
    val component: Component,
    private val onDispose: () -> Unit
) {

    fun dispose() = onDispose()
}
