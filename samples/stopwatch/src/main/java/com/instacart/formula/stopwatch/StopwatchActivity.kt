package com.instacart.formula.stopwatch

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.start
import com.instacart.formula.state
import io.reactivex.disposables.CompositeDisposable

class StopwatchActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stopwatch_activity)

        val renderView = StopwatchRenderView(findViewById(R.id.activity_content))

        val formula = StopwatchFormula()
        disposables.add(formula.start(Unit).subscribe(renderView.renderer::render))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
