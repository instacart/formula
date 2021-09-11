package com.instacart.formula.counter

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CounterActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.counter_activity)

        val renderView = CounterRenderView(findViewById(R.id.activity_content))

        val formula = CounterFormula()

        formula.toFlow().safeCollect { renderView.render(it) }
    }

    fun <T> Flow<T>.safeCollect(block: (T) -> Unit) = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            this@safeCollect.collect {
                block(it)
            }
        }
    }
}
