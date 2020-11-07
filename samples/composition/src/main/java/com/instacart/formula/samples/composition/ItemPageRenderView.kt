package com.instacart.formula.samples.composition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.instacart.formula.Renderer
import com.instacart.formula.RenderView
import com.instacart.formula.samples.composition.item.ItemRenderModel

class ItemPageRenderView(private val root: ViewGroup): RenderView<ItemPageRenderModel> {
    private val itemList: RecyclerView = root.findViewById(R.id.item_list)
    private val itemAdapter = ItemAdapter()

    init {
        itemList.layoutManager = LinearLayoutManager(root.context, VERTICAL, false)
        itemList.adapter = itemAdapter
    }

    override val render: Renderer<ItemPageRenderModel> = Renderer { model ->
        itemAdapter.submitList(model.items)
    }

    class ItemAdapter : ListAdapter<ItemRenderModel, ItemAdapter.ViewHolder>(DiffCallback()) {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameView: TextView = itemView.findViewById(R.id.item_name_view)
            private val countTextView: TextView = itemView.findViewById(R.id.count_text_view)
            private val decrementButton: Button = itemView.findViewById(R.id.decrement_button)
            private val incrementButton: Button = itemView.findViewById(R.id.increment_button)

            fun bind(model: ItemRenderModel) {
                nameView.text = model.name
                countTextView.text = model.displayQuantity
                decrementButton.isEnabled = model.isDecrementEnabled
                decrementButton.setOnClickListener {
                    model.onDecrement()
                }
                incrementButton.setOnClickListener {
                    model.onIncrement()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemRenderModel>() {
        override fun areItemsTheSame(oldItem: ItemRenderModel, newItem: ItemRenderModel): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ItemRenderModel, newItem: ItemRenderModel): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: ItemRenderModel, newItem: ItemRenderModel): Any? {
            return newItem
        }
    }
}
