package com.instacart.formula

import android.view.View
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentLifecycleCallback

object TestFragmentComponent {

    fun <T> create(contract: FragmentContract<T>, view: View): FragmentComponent<T> {
        return FragmentComponent.create(
            renderView = object : RenderView<T> {
                override val render: Render<T> = Render {
                    (view.context as TestFragmentActivity).renderCalls.add(Pair(contract, it))
                }
            },
            lifecycleCallbacks = object : FragmentLifecycleCallback {

            }
        )
    }
}
