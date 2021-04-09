package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.internal.BaseBindingBuilder
import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.rxjava3.core.Observable
import kotlin.reflect.KClass

/**
 * Used to create a [Binding] for [FragmentContract] keys.
 */
class FragmentBindingBuilder<Component> : BaseBindingBuilder<Component>() {
    companion object {

        @PublishedApi
        internal inline fun <Component> build(
            init: FragmentBindingBuilder<Component>.() -> Unit
        ): Bindings<Component> {
            return FragmentBindingBuilder<Component>().apply(init).build()
        }
    }

    /**
     * Binds a contract to specified state management provided by the [Integration].
     *
     * @param type A type of contract to bind
     * @param integration An integration that initializes contracts state management.
     */
    fun <T: FragmentContract<RenderModel>, RenderModel : Any> bind(
        type : KClass<T>,
        integration: Integration<Component, T, RenderModel>
    ) = apply {
        val binding = SingleBinding(type.java, integration)
        bind(binding as Binding<Component>)
    }

    /**
     * Binds a contract to specified state management.
     *
     * @param contract A type of contract to bind
     * @param init A function that initializes contracts state management.
     */
    fun <RenderModel : Any, Contract : FragmentContract<RenderModel>> bind(
        contract: KClass<Contract>,
        init: (Component, Contract) -> Observable<RenderModel>
    ) = apply {
        val integration = object : Integration<Component, Contract, RenderModel>() {
            override fun create(component: Component, key: Contract): Observable<RenderModel> {
                return init(component, key)
            }
        }
        bind(contract, integration)
    }

    /**
     * Binds a contract to specified state management.
     *
     * @param contract A type of contract to bind
     * @param init A function that initializes contracts state management.
     */
    fun <RenderModel : Any, Contract : FragmentContract<RenderModel>> bind(
        contract: KClass<Contract>,
        init: (Contract) -> Observable<RenderModel>
    ) = apply {
        val integration = object : Integration<Component, Contract, RenderModel>() {
            override fun create(component: Component, key: Contract): Observable<RenderModel> {
                return init(key)
            }
        }
        bind(contract, integration)
    }

    /**
     * A convenience inline function that binds integration to a [T] contract.
     *
     * @param integration An integration that initializes contracts state management.
     */
    inline fun <reified T: FragmentContract<RenderModel>, RenderModel : Any> bind(
        integration: Integration<Component, T, RenderModel>
    ) = apply {
        bind(T::class, integration)
    }

    /**
     * A convenience inline function that uses reified type [Contract] as the type of contract
     * and binds state managements provided by the [init] function.
     *
     * @param init A function that initializes contracts state management.
     */
    inline fun <State : Any, reified Contract : FragmentContract<State>> bind(
        noinline init: (Component, Contract) -> Observable<State>
    )= apply {
        bind(Contract::class, init)
    }

    /**
     * A convenience inline function that uses reified type [Contract] as the type of contract
     * and binds state managements provided by the [init] function.
     *
     * @param init A function that initializes contracts state management.
     */
    inline fun <State : Any, reified Contract : FragmentContract<State>> bind(
        noinline init: (Contract) -> Observable<State>
    )= apply {
        bind(Contract::class, init)
    }

    /**
     * Binds a flow integration.
     */
    fun bind(flowIntegration: FlowIntegration<Component, *>) = apply {
        bind(flowIntegration.binding())
    }
}
