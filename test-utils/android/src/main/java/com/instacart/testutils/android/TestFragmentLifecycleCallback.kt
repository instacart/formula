package com.instacart.testutils.android

import android.os.Bundle
import android.view.View
import com.instacart.formula.android.FragmentLifecycleCallback

class TestFragmentLifecycleCallback : FragmentLifecycleCallback {
    var hasOnViewCreated = false
    var hasOnActivityCreated = false
    var hasOnStart = false
    var hasOnResume = false
    var hasOnPauseEvent = false
    var hasOnStop = false
    var hasOnSaveInstanceState = false
    var hasOnDestroyView = false
    var hasCalledLowMemory = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hasOnViewCreated = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hasOnActivityCreated = true
    }

    override fun onStart() {
        super.onStart()
        hasOnStart = true
    }

    override fun onResume() {
        super.onResume()
        hasOnResume = true
    }

    // teardown
    override fun onPause() {
        super.onPause()
        hasOnPauseEvent = true
    }

    override fun onStop() {
        super.onStop()
        hasOnStop = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        hasOnSaveInstanceState = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasOnDestroyView = true
    }

    override fun onLowMemory() {
        super.onLowMemory()
        hasCalledLowMemory = true
    }
}