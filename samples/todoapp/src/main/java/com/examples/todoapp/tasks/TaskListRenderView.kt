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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.examples.todoapp.R
import com.instacart.formula.Renderer
import com.instacart.formula.RenderView

class TaskListRenderView(private val root: View) : RenderView<TaskListRenderModel> {
    private val toolbar: Toolbar = root.findViewById(R.id.toolbar)
    private val filterMenuItem: MenuItem

    private val recyclerView: RecyclerView = root.findViewById(R.id.task_list_recycler)
    private val adapter: TaskListAdapter

    init {
        adapter = TaskListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        toolbar.title = "Tasks"
        toolbar.inflateMenu(R.menu.tasks_list_menu)
        filterMenuItem = toolbar.menu.findItem(R.id.menu_filter)
    }

    override val render: Renderer<TaskListRenderModel> = Renderer { model ->
        val diff = DiffUtil.calculateDiff(TaskDiffCallback(adapter.items, model.items))
        adapter.items = model.items
        diff.dispatchUpdatesTo(adapter)

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

    class TaskDiffCallback(
        private val oldList: List<TaskItemRenderModel>,
        private val newList: List<TaskItemRenderModel>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return Any()
        }
    }

    class TaskItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val completeCheckBox: CheckBox = itemView.findViewById(R.id.complete)

        fun bind(model: TaskItemRenderModel) {
            title.text = model.text
            completeCheckBox.isChecked = model.isSelected
            completeCheckBox.setOnCheckedChangeListener { _, _ ->
                model.onToggle()
            }

            itemView.setOnClickListener {
                model.onClick()
            }
        }
    }

    class TaskListAdapter : RecyclerView.Adapter<TaskItemViewHolder>() {
        var items: List<TaskItemRenderModel> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
            return TaskItemViewHolder(view)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
            holder.bind(items[position])
        }
    }
}
