package com.instacart.formula.navigation

import android.os.Bundle
import com.instacart.formula.android.FormulaAppCompatActivity
import com.instacart.formula.android.FormulaFragment

class NavigationActivity : FormulaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        // Set up navigation effect handler
        NavigationApp.onNavigationEffect = { effect ->
            handleNavigationEffect(effect)
        }

        if (savedInstanceState == null) {
            // Start with fragment 0
            val initialKey = CounterFragmentKey(0)
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(initialKey), initialKey.tag)
                .commit()
        }
    }

    private fun handleNavigationEffect(effect: NavigationEffect) {
        when (effect) {
            is NavigationEffect.NavigateToFragment -> {
                val key = CounterFragmentKey(effect.fragmentId)
                val fragment = FormulaFragment.newInstance(key)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_content, fragment, key.tag)
                    .addToBackStack(key.tag)
                    .commit()
            }

            is NavigationEffect.NavigateBack -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Handle navigation back through our navigation system
        NavigationApp.navigationStore.onEvent(NavigationEvent.NavigateBack)
        NavigationApp.onNavigationEffect?.invoke(NavigationEffect.NavigateBack)
    }
}