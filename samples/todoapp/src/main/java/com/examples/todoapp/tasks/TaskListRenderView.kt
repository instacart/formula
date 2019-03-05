package com.examples.todoapp.tasks

import android.app.Activity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.examples.todoapp.R
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer

class TaskListRenderView(private val root: View) : RenderView<TaskListRenderModel> {
    private val toolbar: Toolbar = root.findViewById(R.id.toolbar)
    private val filterMenuItem: MenuItem

    private val recyclerView: RecyclerView = root.findViewById(R.id.task_list_recycler)
    private val adapter: TaskAdapter

    init {
        adapter = TaskAdapter()
        recyclerView.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        toolbar.title = "Tasks"
        toolbar.inflateMenu(R.menu.tasks_list_menu)
        filterMenuItem = toolbar.menu.findItem(R.id.menu_filter)
    }

    override val renderer: Renderer<TaskListRenderModel> = Renderer.create { model ->
        adapter.items = model.items
        adapter.notifyDataSetChanged()

        filterMenuItem.setOnMenuItemClickListener {
            showFilteringPopUpMenu(model.filterOptions)
            true
        }
    }

    private fun showFilteringPopUpMenu(options: List<TaskFilterRenderModel>) {
        val activity = (root.context as Activity)
        PopupMenu(activity, activity.findViewById(R.id.menu_filter)).apply {
            options.forEach { filter ->
                menu.add(filter.title).setOnMenuItemClickListener {
                    filter.onSelected()
                    true
                }
            }
            show()
        }
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val completeCheckBox: CheckBox = itemView.findViewById(R.id.complete)

        fun bind(model: TaskItemRenderModel) {
            title.text = model.text
            completeCheckBox.isChecked = model.isCompleted
            completeCheckBox.setOnCheckedChangeListener { _, _ ->
                model.onToggleCompleted()
            }

            itemView.setOnClickListener {
                model.onClick()
            }
        }
    }

    class TaskAdapter : RecyclerView.Adapter<TaskViewHolder>() {
        var items: List<TaskItemRenderModel> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
            return TaskViewHolder(view)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            holder.bind(items[position])
        }
    }
}
