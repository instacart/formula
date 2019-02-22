package com.instacart.client.mvi

import android.os.Parcelable
import android.view.View

/**
 * Responsible for providing [ICMviFragmentComponent]
 * that can take [RenderModel] object and render the view using it.
 */
abstract class ICMviFragmentContract<in RenderModel> : Parcelable {
    abstract val tag: String

    /**
     * Layout id that defines the view
     */
    abstract val layoutId: Int

    /**
     * Takes an Android view and creates a [ICMviFragmentComponent]
     */
    abstract fun createComponent(view: View): ICMviFragmentComponent<RenderModel>
}
