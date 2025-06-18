package com.instacart.formula.stopwatch

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class StopwatchActivity : FragmentActivity() {

    private val stopwatchViewModel by viewModels<StopWatchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stopwatch_activity)

        val renderView = StopwatchRenderView(findViewById(R.id.activity_content))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                stopwatchViewModel.viewOutputs.collect {
                    renderView.render(it)
                }
            }
        }
    }
}
