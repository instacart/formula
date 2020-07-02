package com.instacart.formula.test

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.instacart.formula.R
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.FormulaAppCompatActivity

class TestFragmentActivity : FormulaAppCompatActivity() {
    @VisibleForTesting lateinit var initialContract: FragmentContract<*>
    @VisibleForTesting val renderCalls = mutableListOf<Pair<FragmentContract<*>, *>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        if (savedInstanceState == null) {
            val fragment = FormulaFragment.newInstance(initialContract)
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, fragment, initialContract.tag)
                .commit()
        }
    }
}
