package com.instacart.formula.stopwatch

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class StopwatchActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stopwatch_activity)

        val renderView = StopwatchRenderView(findViewById(R.id.activity_content))

        val renderModels: Observable<StopwatchRenderModel> = StopwatchFormula().toObservable()
        disposables.add(renderModels.subscribe(renderView.render))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
