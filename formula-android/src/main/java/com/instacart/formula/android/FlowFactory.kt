package com.instacart.formula.android

import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FragmentBindingBuilder

/**
 * A flow factory enables to group multiple fragments and share state, routers, action handlers
 * and other dependencies between them. A shared [FlowComponent] will be instantiated when user
 * enters one of the features defined within [createFlow]. It will be passed to each
 * [FeatureFactory] as a dependency and will be disposed when user exits the last feature defined
 * in this flow.
 *
 * ```kotlin
 * class AuthFlowFactory : FlowFactory<Dependencies, AuthFlowComponent> {
 *
 *     interface Dependencies {
 *         fun analyticsService(): AnalyticsService
 *         fun apiService(): ApiService
 *         fun authRouter(): AuthRouter
 *     }
 *
 *     override fun createComponent(dependencies: Dependencies): DisposableScope<AuthFlowComponent> {
 *         val flowComponent = AuthFlowComponent(dependencies)
 *         flowComponent.initialize()
 *         return DisposableScope(flowComponent) {
 *             flowComponent.dispose()
 *         }
 *     }
 *
 *     override fun createFlow(): Flow<AuthFlowComponent> {
 *         return Flow.build {
 *             bind(AuthRootFeatureFactory())
 *             bind(LoginFeatureFactory())
 *             bind(SignUpFeatureFactory())
 *             bind(SingleSignOnFeatureFactory())
 *         }
 *     }
 * }
 * ```
 *
 * @param Dependencies A class or an interface provided by the parent that contains dependencies needed by this flow.
 * @param FlowComponent A component that is initialized when user enters this flow and is shared
 * between all the screens within flow. Component will be destroyed when user exits the flow
 */
interface FlowFactory<in Dependencies, FlowComponent> {

    /**
     * Using [dependencies] passed by the parent, this function creates a component used by
     * all features within this flow. This component can be used to share state, routers,
     * action handlers and other dependencies.
     */
    fun createComponent(dependencies: Dependencies): DisposableScope<FlowComponent>

    /**
     * Creates a [Flow] object which contains a sequence of related screens a user may navigate
     * between to perform a task. A shared [FlowComponent] is passed to individual feature factories
     * to help initialize the state management and view rendering logic.
     *
     * ```kotlin
     * override fun createFlow(): Flow<AuthFlowComponent> {
     *     return Flow.build {
     *         bind(AuthRootFeatureFactory())
     *         bind(LoginFeatureFactory())
     *         bind(SignUpFeatureFactory())
     *         bind(SingleSignOnFeatureFactory())
     *      }
     * }
     * ```
     */
    fun createFlow(): Flow<FlowComponent>
}