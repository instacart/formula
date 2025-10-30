package com.instacart.formula.android

import com.instacart.formula.android.internal.FeatureBinding
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.MappedFeatureFactory
import java.lang.IllegalStateException
import kotlin.reflect.KClass

/**
 * Helps to build a [Features] list that binds various route keys to their respective
 * feature factories. Each feature factory has a dependency type that needs to either match
 * [Dependencies] type defined here or map this root dependency type to the custom type.
 */
class FeaturesBuilder<Dependencies> {
    companion object {
        fun <Dependencies> build(
            init: FeaturesBuilder<Dependencies>.() -> Unit
        ): Features<Dependencies> {
            return FeaturesBuilder<Dependencies>().apply(init).build()
        }
    }

    private val types = mutableSetOf<Class<*>>()
    private val bindings: MutableList<FeatureBinding<Dependencies, *>> = mutableListOf()

    /**
     * Binds a [feature factory][FeatureFactory] for a specific [key][type].
     *
     * @param type The class which describes the [key][Key].
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    fun <Key : RouteKey> bind(
        type : KClass<Key>,
        featureFactory: FeatureFactory<Dependencies, Key>,
    ) = apply {
        val binding = FeatureBinding(type.java, featureFactory)
        bind(type.java, binding)
    }

    /**
     * Binds a feature factory for a [Key].
     *
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     */
    inline fun <reified Key: RouteKey> bind(
        featureFactory: FeatureFactory<Dependencies, Key>
    ) = apply {
        bind(Key::class, featureFactory)
    }

    /**
     * A convenience inline function that binds a feature factory for a specific [key][Key].
     *
     * @param featureFactory Feature factory that provides state observable and view rendering logic.
     * @param toDependencies Maps [Dependencies] to feature factory [dependencies][CustomDependencyType].
     */
    inline fun <CustomDependencyType, reified Key: RouteKey> bind(
        featureFactory: FeatureFactory<CustomDependencyType, Key>,
        noinline toDependencies: (Dependencies) -> CustomDependencyType
    ) = apply {
        val mapped = MappedFeatureFactory(
            delegate = featureFactory,
            toDependencies = toDependencies,
        )
        bind(Key::class, mapped)
    }

    @PublishedApi
    internal fun build(): Features<Dependencies> {
        return Features(bindings)
    }

    private fun bind(type: Class<*>, binding: FeatureBinding<Dependencies, *>) = apply {
        if (types.contains(type)) {
            throw IllegalStateException("Binding for $type already exists")
        }
        types += type
        bindings += binding
    }
}
