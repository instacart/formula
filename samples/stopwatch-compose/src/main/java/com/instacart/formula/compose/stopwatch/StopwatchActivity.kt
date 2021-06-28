package com.instacart.formula.compose.stopwatch

import android.os.Bundle
import com.instacart.formula.android.FormulaAppCompatActivity
import com.instacart.formula.android.FormulaFragment

class StopwatchActivity : FormulaAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stopwatch_activity)

        if (savedInstanceState == null) {
            val key = StopwatchKey()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(key), key.tag)
                .commit()
        }
    }
}