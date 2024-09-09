package com.instacart.formula.android

import android.os.Bundle
import android.view.View

/**
 * The typical [Fragment lifecycle](https://developer.android.com/guide/components/fragments) callbacks, which you can optionally hook in to.
 * Since components are created when a view is provided in [onViewCreated] and destroyed when a view is destroyed in [onDestroyView],
 * we cannot provide callbacks to [androidx.fragment.app.Fragment.onAttach], [androidx.fragment.app.Fragment.onCreate],
 * [androidx.fragment.app.Fragment.onDestroy] or [androidx.fragment.app.Fragment.onDetach]
 */
interface FragmentLifecycleCallback {
    companion object {
        internal val NO_OP = object : FragmentLifecycleCallback {}
    }

    /**
     * See [androidx.fragment.app.Fragment.onViewCreated]
     */
    fun onViewCreated(view: View, savedInstanceState: Bundle?) = Unit

    /**
     * See [androidx.fragment.app.Fragment.onActivityCreated]
     */
    fun onActivityCreated(savedInstanceState: Bundle?) = Unit

    /**
     * See [androidx.fragment.app.Fragment.onStart]
     */
    fun onStart() = Unit

    /**
     * See [androidx.fragment.app.Fragment.onResume]
     */
    fun onResume() = Unit

    /**
     * See [androidx.fragment.app.Fragment.onPause]
     */
    fun onPause() = Unit

    /**
     * See [androidx.fragment.app.Fragment.onStop]
     */
    fun onStop() = Unit

    /**
     * See [androidx.fragment.app.Fragment.onSaveInstanceState]
     */
    fun onSaveInstanceState(outState: Bundle) = Unit

    /**
     * See [androidx.fragment.app.Fragment.onLowMemory]
     */
    fun onLowMemory() = Unit

    /**
     * See [androidx.fragment.app.Fragment.onDestroyView]
     */
    fun onDestroyView() = Unit
}
