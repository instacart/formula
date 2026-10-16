package com.instacart.formula.android.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentInspector
import androidx.lifecycle.Lifecycle

/** Tracks legacy Fragment lifecycle state for [ActivityStoreContextImpl]. */
internal class FragmentLifecycleTracker(
    activity: FragmentActivity,
    private val onLifecycleStateChanged: (String, Lifecycle.State) -> Unit,
) {
    private val fragmentManager = activity.supportFragmentManager

    private val callbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?,
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            notifyLifecycleStateChanged(f, Lifecycle.State.CREATED)
        }

        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            super.onFragmentStarted(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.STARTED)
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            super.onFragmentResumed(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.RESUMED)
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
            super.onFragmentPaused(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.STARTED)
        }

        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
            super.onFragmentStopped(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.CREATED)
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.DESTROYED)
        }

        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            super.onFragmentDetached(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.DESTROYED)
        }
    }

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(callbacks, false)
    }

    fun dispose() {
        fragmentManager.unregisterFragmentLifecycleCallbacks(callbacks)
    }

    private fun notifyLifecycleStateChanged(fragment: Fragment, state: Lifecycle.State) {
        if (shouldTrack(fragment)) {
            onLifecycleStateChanged(fragment.tag.orEmpty(), state)
        }
    }

    private fun shouldTrack(fragment: Fragment): Boolean {
        return !fragment.retainInstance && !FragmentInspector.isHeadless(fragment)
    }
}
