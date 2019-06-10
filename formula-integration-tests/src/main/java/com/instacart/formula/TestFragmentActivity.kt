package com.instacart.formula

import android.os.Bundle
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.FormulaAppCompatActivity

class TestFragmentActivity : FormulaAppCompatActivity() {
    // Exposed for testing purposes.
    lateinit var initialContract: FragmentContract<*>
    val renderCalls = mutableListOf<Pair<FragmentContract<*>, *>>()

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
