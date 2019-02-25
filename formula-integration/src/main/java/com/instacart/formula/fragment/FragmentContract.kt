package com.instacart.formula.fragment

import android.os.Parcelable
import android.view.View

/**
 * Responsible for providing [FragmentComponent]
 * that can take [RenderModel] object and render the view using it.
 */
abstract class FragmentContract<in RenderModel> : Parcelable {
    abstract val tag: String

    /**
     * Layout id that defines the view
     */
    abstract val layoutId: Int

    /**
     * Takes an Android view and creates a [FragmentComponent]
     */
    abstract fun createComponent(view: View): FragmentComponent<RenderModel>
}
