package com.instacart.formula.activity

import android.content.Intent

data class ActivityResult(
    val requestCode: Int,
    val resultCode: Int,
    val data: Intent?
)
