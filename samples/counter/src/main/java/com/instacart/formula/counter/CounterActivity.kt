package com.instacart.formula.counter

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.start
import io.reactivex.rxjava3.disposables.CompositeDisposable

class CounterActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.counter_activity)

        val renderView = CounterRenderView(findViewById(R.id.activity_content))

        val formula = CounterFormula()
        disposables.add(formula.start().subscribe(renderView.render))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
