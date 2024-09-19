package com.instacart.formula.test

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.FormulaAppCompatActivity
import com.instacart.testutils.android.R

class TestFragmentActivity : FormulaAppCompatActivity() {
    @VisibleForTesting val renderCalls = mutableListOf<Pair<FragmentKey, *>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
    }
}
