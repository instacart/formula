package com.examples.todoapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.reactivex.disposables.CompositeDisposable

class TodoActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_activity)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
