package com.instacart.formula.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.instacart.formula.integration.LifecycleEvent
import com.instacart.formula.internal.mapNotNull
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.MainThreadDisposable

/**
 * Helps activities track active MVI contracts.
 */
object FormulaActivity {
    /**
     * returns a [Flowable] that will emit [FragmentEvent]s for non retained, non glide manager fragments
     */
    private fun fragmentLifecycleEvents(
        activity: FragmentActivity,
        shouldTrack: (Fragment) -> Boolean = { true }
    ): Flowable<FragmentEvent> {
        return Flowable.create({ emitter ->
            val listener = object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                    if (!f.retainInstance && shouldTrack(f)) {
                        emitter.onNext(FragmentEvent.Attached(f))
                    }
                }

                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    if (!f.retainInstance && shouldTrack(f)) {
                        emitter.onNext(FragmentEvent.Detached(f))
                    }
                }
            }

            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(listener, false)

            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(listener)
                }
            })
        }, BackpressureStrategy.BUFFER)
    }

    /**
     * Must subscribe to the state before calling Activity.super.onCreate(),
     * otherwise you might miss fragment event
     */
    @JvmStatic fun lifecycleEffects(
        activity: FragmentActivity,
        shouldTrack: (Fragment) -> Boolean = { true }
    ): Flowable<LifecycleEvent<FragmentContract<*>>> {
        return fragmentLifecycleEvents(activity, shouldTrack)
            .mapNotNull { event ->
                val fragment = event.fragment as? BaseFormulaFragment<*>
                val contract = fragment?.getMviContract() ?: EmptyFragmentContract(event.fragment.tag.orEmpty())
                contract.let { it: FragmentContract<*> ->
                    when (event) {
                        is FragmentEvent.Attached -> LifecycleEvent.Attach(it)
                        is FragmentEvent.Detached -> {
                            // Only trigger detach, when fragment is actually being removed from the backstack
                            if (event.fragment.isRemoving) {
                                LifecycleEvent.Detach(it, fragment?.currentState())
                            } else {
                                null
                            }
                        }
                    }
                }
            }
    }
}
