package com.instacart.formula.android

import com.instacart.formula.android.internal.Binding
import com.instacart.formula.android.internal.Bindings
import com.instacart.formula.android.internal.FunctionUtils
import com.instacart.formula.android.views.FragmentContractViewFactory
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.android.internal.FeatureBinding
import io.reactivex.rxjava3.core.Observable
import java.lang.IllegalStateException
import kotlin.reflect.KClass

/**
 * Used to create a [Binding] for [FragmentKey] keys.
 */
class FragmentBindingBuilder<Component> {
    companion object {

        @PublishedApi
        internal inline fun <Component> build(
            init: FragmentBindingBuilder<Component>.() -> Unit
        ): Bindings<Component> {
            return FragmentBindingBuilder<Component>().apply(init).build()
        }
    }

    private val types = mutableSetOf<Class<*>>()
    private val bindings: MutableList<Binding<Component>> = mutableListOf()

    /**
     * Binds a [feature factory][FeatureFactory] for a specific [key][type].
     *
     * @param type The class which describes the [key][Key].
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     * @param toDependencies Maps [Component] to feature factory [dependencies][Dependencies].
     */
    fun <Dependencies, Key : FragmentKey> bind(
        type : KClass<Key>,
        featureFactory: FeatureFactory<Dependencies, Key>,
        toDependencies: (Component) -> Dependencies
    ) = apply {
        val binding = FeatureBinding(type.java, featureFactory, toDependencies)
        bind(binding as Binding<Component>)
    }

    /**
     * Binds a [feature factory][FeatureFactory] for a specific [key][type].
     *
     * @param type The class which describes the [key][Key].
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    fun <Key : FragmentKey> bind(
        type : KClass<Key>,
        featureFactory: FeatureFactory<Component, Key>
    ) = apply {
        bind(type, featureFactory, FunctionUtils.identity())
    }

    /**
     * A convenience inline function that binds a feature factory for a specific [key][Key].
     *
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    inline fun <reified Key: FragmentKey> bind(
        featureFactory: FeatureFactory<Component, Key>
    ) = apply {
        bind(Key::class, featureFactory)
    }

    /**
     * A convenience inline function that binds a feature factory for a specific [key][Key].
     *
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     * @param toDependencies Maps [Component] to feature factory [dependencies][Dependencies].
     */
    inline fun <Dependencies, reified Key: FragmentKey> bind(
        featureFactory: FeatureFactory<Dependencies, Key>,
        noinline toDependencies: (Component) -> Dependencies
    ) = apply {
        bind(Key::class, featureFactory, toDependencies)
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

    /**
     * Binds a flow factory.
     */
    fun <Dependencies> bind(
        flowFactory: FlowFactory<Dependencies, *>,
        toDependencies: (Component) -> Dependencies
    ) = apply {
        val binding = Binding.composite(flowFactory, toDependencies)
        bind(binding)
    }

    @PublishedApi
    internal fun build(): Bindings<Component> {
        return Bindings(
            types = types,
            bindings = bindings
        )
    }

    private fun bind(binding: Binding<Component>) = apply {
        binding.types().forEach {
            if (types.contains(it)) {
                throw IllegalStateException("Binding for $it already exists")
            }
            types += it
        }

        bindings += binding
    }
}
