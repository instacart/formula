package com.instacart.formula.navigation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.instacart.formula.android.FormulaAppCompatActivity
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.FragmentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NavigationActivity : FormulaAppCompatActivity() {

    lateinit var fragmentState: Flow<FragmentState>

    private val viewModel: NavigationViewModel by viewModels {
        NavigationViewModelFactory(fragmentState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        // Collect navigation events from ViewModel and handle them in the Activity
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvents.collect { action ->
                    handleNavigationAction(action)
                }
            }
        }
    }

    fun getNavigationOutput(): NavigationActivityFormula.Output {
        return viewModel.state.value
    }

    private fun handleNavigationAction(action: NavigationAction) {
        when (action) {
            is NavigationAction.NavigateToFragment -> {
                val key = CounterFragmentKey(action.fragmentId)
                val fragment = FormulaFragment.newInstance(key)

                with(supportFragmentManager.beginTransaction()) {
                    replace(R.id.activity_content, fragment, key.tag)
                    addToBackStack(key.tag)
                    commit()
                }
            }

            is NavigationAction.NavigateBack -> {
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Handle navigation back through our formula
        viewModel.state.value.onNavigateBack()
    }
}