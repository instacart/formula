package com.instacart.formula.stopwatch

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.instacart.formula.Renderer
import com.instacart.formula.RenderView

class StopwatchRenderView(root: ViewGroup) : RenderView<StopwatchRenderModel> {
    private val timePassed: TextView = root.findViewById(R.id.time_passed_text_view)

    override val render: Renderer<StopwatchRenderModel> = Renderer { model ->
        timePassed.text = model.timePassed
    }
}
