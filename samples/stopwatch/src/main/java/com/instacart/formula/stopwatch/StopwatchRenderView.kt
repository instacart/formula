package com.instacart.formula.stopwatch

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.instacart.formula.Render
import com.instacart.formula.RenderView

class StopwatchRenderView(root: ViewGroup): RenderView<StopwatchRenderModel> {
    private val timePassed: TextView = root.findViewById(R.id.time_passed_text_view)
    private val startStopButton: Button = root.findViewById(R.id.start_stop_button)
    private val resetButton: Button = root.findViewById(R.id.reset_button)

    override val render: Render<StopwatchRenderModel> = Render { model ->
        timePassed.text = model.timePassed

        startStopButton.text = model.startStopButton.text
        startStopButton.setOnClickListener {
            model.startStopButton.onSelected()
        }

        resetButton.text = model.resetButton.text
        resetButton.setOnClickListener {
            model.resetButton.onSelected()
        }
    }
}
