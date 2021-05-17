package com.instacart.formula.test

import android.view.View
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer
import com.instacart.formula.android.FragmentComponent
import com.instacart.formula.android.FragmentContract
import com.instacart.formula.android.FragmentLifecycleCallback

object TestFragmentComponent {

    fun <T> create(contract: FragmentContract<T>, view: View): FragmentComponent<T> {
        return FragmentComponent.create(
            renderView = object : RenderView<T> {
                override val render: Renderer<T> = Renderer {
                    (view.context as TestFragmentActivity).renderCalls.add(Pair(contract, it))
                }
            },
            lifecycleCallbacks = object : FragmentLifecycleCallback {

            }
        )
    }
}
