package com.instacart.formula.samples.networkstate

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.start
import io.reactivex.rxjava3.disposables.CompositeDisposable

class NetworkStateActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val networkStatusTextView = findViewById<TextView>(R.id.network_status_text)
        val renderModels = NetworkStateFormula(NetworkStateStreamImpl(application)).start()
        disposables.add(renderModels.subscribe {
            networkStatusTextView.text = it.status
        })
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
