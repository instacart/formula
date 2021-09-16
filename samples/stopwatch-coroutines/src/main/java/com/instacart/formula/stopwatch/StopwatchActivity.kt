package com.instacart.formula.stopwatch

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StopwatchActivity : FragmentActivity() {

    private val counterViewModel by viewModels<StopWatchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stopwatch_activity)

        val renderView = StopwatchRenderView(findViewById(R.id.activity_content))

        val renderModels = counterViewModel.renderModelFlow

        renderModels.safeCollect { renderView.render(it) }
    }

    fun <T> Flow<T>.safeCollect(block: (T) -> Unit) = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            this@safeCollect.collect {
                block(it)
            }
        }
    }

}
