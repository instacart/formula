package com.instacart.formula.samples.composition

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val renderView = ItemPageRenderView(findViewById(R.id.activity_content))

        val formula = ItemPageFormula()
        disposables.add(formula.toObservable().subscribe(renderView.render))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
