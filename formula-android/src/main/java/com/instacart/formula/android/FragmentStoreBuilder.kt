package com.instacart.formula.android

import com.instacart.formula.android.internal.Binding
import com.instacart.formula.android.internal.Bindings
import com.instacart.formula.android.internal.FunctionUtils
import com.instacart.formula.android.internal.FeatureBinding
import java.lang.IllegalStateException
import kotlin.reflect.KClass

/**
 * A class used by [FragmentFlowStore] and [Flow] to register [fragment keys][FragmentKey] and their
 * feature factories.
 */
class FragmentStoreBuilder<Component> {
    companion object {

        @PublishedApi
        internal inline fun <Component> build(
            init: FragmentStoreBuilder<Component>.() -> Unit
        ): Bindings<Component> {
            return FragmentStoreBuilder<Component>().apply(init).build()
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
     */
    inline fun <reified Key: FragmentKey> bind(
        crossinline initFeature: (Component, Key) -> Feature<*>,
    ) = apply {
        val factory = object : FeatureFactory<Component, Key> {
            override fun initialize(dependencies: Component, key: Key): Feature<*> {
                return initFeature(dependencies, key)
            }
        }
        bind(Key::class, factory)
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
