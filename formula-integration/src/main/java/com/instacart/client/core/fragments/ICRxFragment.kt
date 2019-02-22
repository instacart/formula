package com.instacart.client.core.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.MainThreadDisposable

object ICRxFragment {

    /**
     * returns a [Flowable] that will emit [ICFragmentEvent]s for non retained, non glide manager fragments
     */
    @JvmStatic fun fragmentLifecycleEvents(
        activity: FragmentActivity,
        shouldTrack: (Fragment) -> Boolean = { true }
    ): Flowable<ICFragmentEvent> {
        return Flowable.create({ emitter ->
            val listener = object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                    if (!f.retainInstance && shouldTrack(f)) {
                        emitter.onNext(ICFragmentEvent.Attached(f))
                    }
                }

                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    if (!f.retainInstance && shouldTrack(f)) {
                        emitter.onNext(ICFragmentEvent.Detached(f))
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
}
