package com.instacart.formula.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentInspector
import androidx.fragment.app.FragmentManager
import com.instacart.formula.fragment.FragmentLifecycle.lifecycleEvents
import com.instacart.formula.integration.LifecycleEvent
import io.reactivex.Observable
import io.reactivex.android.MainThreadDisposable

/**
 * Provides utility method [lifecycleEvents] to track what fragments are added and removed from the backstack.
 */
object FragmentLifecycle {

    private fun shouldTrack(fragment: Fragment): Boolean {
        return !fragment.retainInstance && !FragmentInspector.isHeadless(fragment)
    }

    /**
     * Must subscribe to the state before calling Activity.super.onCreate(),
     * otherwise you might miss fragment event
     */
    fun lifecycleEvents(activity: FragmentActivity): Observable<LifecycleEvent<FragmentContract<*>>> {
        return Observable.create { emitter ->
            val listener = object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                    if (shouldTrack(f)) {
                        val fragment = f as? BaseFormulaFragment<*>
                        val contract = fragment?.getFragmentContract() ?: EmptyFragmentContract(f.tag.orEmpty())
                        emitter.onNext(LifecycleEvent.Added(contract))
                    }
                }

                override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                    super.onFragmentStopped(fm, f)
                    // Only trigger detach, when fragment is actually being removed from the backstack
                    if (shouldTrack(f) && f.isRemoving) {
                        val fragment = f as? BaseFormulaFragment<*>
                        val contract = fragment?.getFragmentContract() ?: EmptyFragmentContract(f.tag.orEmpty())
                        emitter.onNext(LifecycleEvent.Removed(contract, fragment?.currentState()))
                    }
                }
            }

            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(listener, false)

            emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() {
                    activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(listener)
                }
            })
        }
    }
}
