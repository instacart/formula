package com.instacart.formula.test

import android.os.Bundle
import android.view.View
import com.instacart.formula.R
import com.instacart.formula.android.FragmentComponent
import com.instacart.formula.android.FragmentContract
import com.instacart.formula.android.FragmentLifecycleCallback
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestLifecycleContract(
    override val tag: String = "task list",
    override val layoutId: Int = R.layout.test_empty_layout
) : FragmentContract<Any>() {

    @IgnoredOnParcel
    var hasOnViewCreated = false
    @IgnoredOnParcel
    var hasOnActivityCreated = false
    @IgnoredOnParcel
    var hasOnStart = false
    @IgnoredOnParcel
    var hasOnResume = false
    @IgnoredOnParcel
    var hasOnPauseEvent = false
    @IgnoredOnParcel
    var hasOnStop = false
    @IgnoredOnParcel
    var hasOnSaveInstanceState = false
    @IgnoredOnParcel
    var hasOnDestroyView = false

    override fun createComponent(view: View): FragmentComponent<Any> {
        return FragmentComponent.create(render = {}, lifecycleCallbacks = object : FragmentLifecycleCallback {

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
        })
    }
}
