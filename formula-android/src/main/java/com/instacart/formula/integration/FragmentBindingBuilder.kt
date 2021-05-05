package com.instacart.formula.integration

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FlowFactory
import com.instacart.formula.android.views.FragmentContractViewFactory
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentKey
import com.instacart.formula.integration.internal.BaseBindingBuilder
import com.instacart.formula.integration.internal.FeatureBinding
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
     * Binds a [feature factory][FeatureFactory] for a specific [key][type].
     *
     * @param type The class which describes the [key][T].
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    fun <T : FragmentKey> bind(
        type : KClass<T>,
        featureFactory: FeatureFactory<Component, T>
    ) = apply {
        val binding = FeatureBinding(type.java, featureFactory as FeatureFactory<Component, FragmentKey>)
        bind(binding as Binding<Component>)
    }

    /**
     * A convenience inline function that binds a feature factory for a specific [key][T].
     *
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    inline fun <reified T: FragmentKey> bind(
        featureFactory: FeatureFactory<Component, T>
    ) = apply {
        bind(T::class, featureFactory)
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
        val featureFactory = object : FeatureFactory<Component, T> {
            override fun initialize(dependencies: Component, key: T): Feature<*> {
                return Feature(
                    state = integration.create(dependencies, key),
                    viewFactory = FragmentContractViewFactory(key)
                )
            }
        }
        bind(type, featureFactory)
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
     * Binds a flow factory.
     */
    fun bind(flowFactory: FlowFactory<Component, *>) = apply {
        val binding = Binding.composite(flowFactory)
        bind(binding)
    }
}
