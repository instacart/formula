package com.instacart.testutils.android

import android.os.Bundle
import com.instacart.formula.android.FormulaAppCompatActivity

class TestFormulaActivity : FormulaAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
    }
}
