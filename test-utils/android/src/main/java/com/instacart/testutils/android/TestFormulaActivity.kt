package com.instacart.testutils.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instacart.formula.FormulaAndroid

class TestFormulaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
    }

}
