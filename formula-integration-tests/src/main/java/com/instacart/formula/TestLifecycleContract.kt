package com.instacart.formula

import android.os.Bundle
import android.view.View
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentLifecycleCallback
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestLifecycleContract(
    override val tag: String = "task list",
    override val layoutId: Int = R.layout.test_empty_layout
) : FragmentContract<String>() {

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

    override fun createComponent(view: View): FragmentComponent<String> {
        return FragmentComponent.noOp(object : FragmentLifecycleCallback {

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
