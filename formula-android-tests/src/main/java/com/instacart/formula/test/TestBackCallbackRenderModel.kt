package com.instacart.formula.test

import com.instacart.formula.android.BackCallback

data class TestBackCallbackRenderModel(
    private val onBackPressed: () -> Unit,
    val blockBackCallback: Boolean = false,
) : BackCallback {
    override fun onBackPressed(): Boolean {
        this.onBackPressed.invoke()
        return blockBackCallback
    }
}