package com.instacart.formula.activity

import android.content.Intent

/**
 * This data class represents [android.app.Activity.onActivityResult] event.
 */
data class ActivityResult(
    val requestCode: Int,
    val resultCode: Int,
    val data: Intent?
)
