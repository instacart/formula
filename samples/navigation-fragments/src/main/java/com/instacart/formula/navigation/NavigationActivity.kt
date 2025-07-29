package com.instacart.formula.navigation

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.instacart.formula.android.FormulaAppCompatActivity
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.runAsStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NavigationActivity : FormulaAppCompatActivity() {

    private val navigationFormula = NavigationActivityFormula()
    private lateinit var navigationState: StateFlow<NavigationActivityFormula.Output>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        // Initialize the navigation formula
        val input = NavigationActivityFormula.Input(
            onNavigation = ::handleNavigationAction,
        )

        navigationState = navigationFormula.runAsStateFlow(lifecycleScope, input)

        if (savedInstanceState == null) {
            // Start with fragment 0
            val initialKey = CounterFragmentKey(0)
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(initialKey), initialKey.tag)
                .commit()
        }
    }

    fun getNavigationOutput(): NavigationActivityFormula.Output {
        return navigationState.value
    }

    private fun handleNavigationAction(action: NavigationAction) {
        when (action) {
            is NavigationAction.NavigateToFragment -> {
                val key = CounterFragmentKey(action.fragmentId)
                val fragment = FormulaFragment.newInstance(key)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_content, fragment, key.tag)
                    .addToBackStack(key.tag)
                    .commit()
            }

            is NavigationAction.NavigateBack -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Handle navigation back through our formula
        navigationState.value.onNavigateBack()
    }
}