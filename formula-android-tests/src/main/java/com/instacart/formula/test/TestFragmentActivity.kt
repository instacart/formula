package com.instacart.formula.test

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.R
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentContract
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.FormulaAppCompatActivity

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
                .addToBackStack(initialContract.tag)
                .commit()
        }
    }

    fun navigateTo(key: FragmentKey) {
        val entryIndex = supportFragmentManager.backStackEntryCount - 1
        val fragment = if (entryIndex >= 0) {
            val entry = supportFragmentManager.getBackStackEntryAt(entryIndex)
            supportFragmentManager.findFragmentByTag(entry.name)
        } else {
            null
        }

        supportFragmentManager.beginTransaction().apply {
            if (fragment != null) {
                remove(fragment)
            }
            add(R.id.activity_content, FormulaFragment.newInstance(key), key.tag)
            addToBackStack(key.tag)
        }.commit()
    }

    override fun onBackPressed() {
        if (!FormulaAndroid.onBackPressed(this)) {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStackImmediate()
            } else {
                finish()
            }
        }
    }
}
