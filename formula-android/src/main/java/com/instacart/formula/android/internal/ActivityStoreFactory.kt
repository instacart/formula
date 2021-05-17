package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.ActivityConfigurator
import com.instacart.formula.android.FragmentEnvironment
import kotlin.reflect.KClass

internal class ActivityStoreFactory internal constructor(
    private val bindings: Map<KClass<*>, ActivityConfigurator.Binding<*>>,
    private val environment: FragmentEnvironment
) {
    companion object {
        operator fun invoke(
            environment: FragmentEnvironment,
            activities: ActivityConfigurator.() -> Unit
        ): ActivityStoreFactory {
            val bindings = ActivityConfigurator().apply(activities).bindings
            return ActivityStoreFactory(bindings, environment)
        }
    }

    internal fun <A : FragmentActivity> init(activity: A): ActivityManager<A>? {
        val initializer = bindings[activity::class] as? ActivityConfigurator.Binding<A>
            ?: return null

        val activityDelegate = ActivityStoreContextImpl<A>()
        return initializer.init.invoke(activityDelegate)?.let { store ->
            ActivityManager(
                environment = environment,
                delegate = activityDelegate,
                store = store
            )
        }
    }
}
