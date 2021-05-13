package com.instacart.formula.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instacart.formula.FormulaAndroid

open class FormulaAppCompatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FormulaAndroid.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (!FormulaAndroid.onBackPressed(this)) {
            super.onBackPressed()
        }
    }

    /**
     * Since we override [onBackPressed], this method allows to bypass our extra logic.
     */
    fun navigateBack() {
        super.onBackPressed()
    }
}
