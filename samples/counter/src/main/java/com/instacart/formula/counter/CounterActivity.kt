package com.instacart.formula.counter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.Logger
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class CounterActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.counter_activity)

        val renderView = CounterRenderView(findViewById(R.id.activity_content))

        val logger = object : Logger {
            override fun logEvent(event: String) {
                Log.d("Formula", event)
            }
        }
        val formula = CounterFormula()
        disposables.add(formula.toObservable(logger).subscribe(renderView.render))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
