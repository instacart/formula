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

    fun shouldTrack(fragment: Fragment): Boolean {
        return !fragment.retainInstance && !FragmentInspector.isHeadless(fragment)
    }

    private fun isKept(fragmentManager: FragmentManager, fragment: Fragment): Boolean {
        return !fragment.isRemoving
    }

    /**
     * Must subscribe to the state before calling Activity.super.onCreate(),
     * otherwise you might miss fragment event
     */
    fun lifecycleEvents(activity: FragmentActivity): Observable<FragmentLifecycleEvent> {
        return Observable.create { emitter ->
            val listener = object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                    if (shouldTrack(f)) {
                        val fragment = f as? BaseFormulaFragment<*>
                        val contract = fragment?.getFragmentContract() ?: EmptyFragmentContract(f.tag.orEmpty())
                        emitter.onNext(LifecycleEvent.Added(contract))
                    }
                }

                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    super.onFragmentDetached(fm, f)
                    // Only trigger detach, when fragment is actually being removed from the backstack
                    if (shouldTrack(f) && !isKept(fm, f)) {
                        emitter.onNext(createRemovedEvent(f))
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

    internal fun createRemovedEvent(f: Fragment): LifecycleEvent.Removed<FragmentContract<Nothing>> {
        val fragment = f as? BaseFormulaFragment<*>
        val contract = fragment?.getFragmentContract() ?: EmptyFragmentContract(f.tag.orEmpty())
        return LifecycleEvent.Removed(contract, fragment?.currentState())
    }
}
