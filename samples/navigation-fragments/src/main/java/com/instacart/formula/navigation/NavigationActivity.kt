package com.instacart.formula.navigation

import android.os.Bundle
import com.instacart.formula.android.FormulaAppCompatActivity
import com.instacart.formula.android.FormulaFragment

class NavigationActivity : FormulaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        // Display initial fragment
        if (savedInstanceState == null) {
            showNextFragment(id = 0)
        }
    }

    fun showNextFragment(id: Int) {
        val key = CounterRouteKey(id)
        val fragment = FormulaFragment.newInstance(key)

        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.activity_content, fragment, key.tag)
            addToBackStack(key.tag)
            commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}