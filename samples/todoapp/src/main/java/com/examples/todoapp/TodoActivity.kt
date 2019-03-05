package com.examples.todoapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.examples.todoapp.tasks.TaskListContract
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.integration.FragmentFlowRenderView
import io.reactivex.disposables.CompositeDisposable

class TodoActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    private lateinit var fragmentRenderView: FragmentFlowRenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        val component = TodoApp.component(this)

        fragmentRenderView = FragmentFlowRenderView(this, onLifecycleEvent = component::onLifecycleEvent)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_activity)

        disposables.add(component.state().subscribe(fragmentRenderView.renderer::render))

        if (savedInstanceState == null) {
            val contract = TaskListContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
        }
    }

    override fun onDestroy() {
        fragmentRenderView.dispose()
        disposables.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!fragmentRenderView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
