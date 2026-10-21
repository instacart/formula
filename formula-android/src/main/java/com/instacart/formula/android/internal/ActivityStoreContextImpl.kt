package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.ActivityStoreContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of [ActivityStoreContext].
 */
internal class ActivityStoreContextImpl<Activity : FragmentActivity> : ActivityStoreContext<Activity>() {

    private var activity: Activity? = null
    private var hasStarted: Boolean = false
    private val lifecycleStates = MutableStateFlow(Lifecycle.State.INITIALIZED)

    override fun activityLifecycleState(): StateFlow<Lifecycle.State> = lifecycleStates

    override fun send(effect: Activity.() -> Unit) {
        // We allow emitting effects only after activity has started
        if (Utils.isMainThread()) {
           startedActivity()?.effect()
        } else {
            Utils.mainThreadHandler.post {
                startedActivity()?.effect()
            }
        }
    }

    fun onLifecycleStateChanged(state: Lifecycle.State) {
        lifecycleStates.tryEmit(state)
    }

    fun attachActivity(activity: Activity) {
        hasStarted = false
        this.activity = activity
    }

    fun onActivityStarted(activity: Activity) {
        hasStarted = true
    }

    fun detachActivity(activity: Activity) {
        if (this.activity == activity) {
            this.activity = null
        }
    }

    internal fun startedActivity(): Activity? = activity.takeIf { hasStarted }
}
