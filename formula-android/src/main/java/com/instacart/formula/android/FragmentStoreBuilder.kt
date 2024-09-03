package com.instacart.formula.android

import com.instacart.formula.android.internal.FeatureBinding
import com.instacart.formula.android.internal.MappedFeatureFactory
import java.lang.IllegalStateException
import kotlin.reflect.KClass

/**
 * A class used by [FragmentFlowStore] to register [fragment keys][FragmentKey] and their
 * feature factories.
 */
class FragmentStoreBuilder<Component> {
    companion object {
        @PublishedApi
        internal inline fun <Component> build(
            init: FragmentStoreBuilder<Component>.() -> Unit
        ): List<FeatureBinding<Component, *>> {
            return FragmentStoreBuilder<Component>().apply(init).build()
        }
    }

    private val types = mutableSetOf<Class<*>>()
    private val bindings: MutableList<FeatureBinding<Component, *>> = mutableListOf()

    /**
     * Binds a [feature factory][FeatureFactory] for a specific [key][type].
     *
     * @param type The class which describes the [key][Key].
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    fun <Key : FragmentKey> bind(
        type : KClass<Key>,
        featureFactory: FeatureFactory<Component, Key>,
    ) = apply {
        val binding = FeatureBinding(type.java, featureFactory)
        bind(type.java, binding)
    }

    /**
     * Binds a feature factory for a [Key].
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
        val mapped = MappedFeatureFactory(
            delegate = featureFactory,
            toDependencies = toDependencies,
        )
        bind(Key::class, mapped)
    }

    @PublishedApi
    internal fun build(): List<FeatureBinding<Component, *>> {
        return bindings
    }

    private fun bind(type: Class<*>, binding: FeatureBinding<Component, *>) = apply {
        if (types.contains(type)) {
            throw IllegalStateException("Binding for $type already exists")
        }
        types += type
        bindings += binding
    }
}
