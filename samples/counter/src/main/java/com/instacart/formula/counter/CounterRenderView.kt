package com.instacart.formula.counter

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.instacart.formula.Renderer
import com.instacart.formula.RenderView

class CounterRenderView(private val root: ViewGroup): RenderView<CounterRenderModel> {
    private val decrementButton: Button = root.findViewById(R.id.decrement_button)
    private val incrementButton: Button = root.findViewById(R.id.increment_button)
    private val countTextView: TextView = root.findViewById(R.id.count_text_view)

    override val render: Renderer<CounterRenderModel> = Renderer { model ->
        countTextView.text = model.count
        decrementButton.setOnClickListener {
            model.onDecrement()
        }
        incrementButton.setOnClickListener {
            model.onIncrement()
        }
    }
}
