package com.instacart.formula.test

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hasOnViewCreated = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        hasOnActivityCreated = true
    }

    override fun onStart() {
        hasOnStart = true
    }

    override fun onResume() {
        hasOnResume = true
    }

    // teardown
    override fun onPause() {
        hasOnPauseEvent = true
    }

    override fun onStop() {
        hasOnStop = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        hasOnSaveInstanceState = true
    }

    override fun onDestroyView() {
        hasOnDestroyView
    }
}