package com.dertefter.neticlient.ui.documents

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.ItemDocumentBinding
import com.dertefter.neticore.features.documents.model.DocumentsItem
import com.google.android.material.color.MaterialColors

class DocumentsAdapter(
    private var itemList: List<DocumentsItem> = emptyList(),
    private val onItemClick: (DocumentsItem) -> Unit
) : RecyclerView.Adapter<DocumentsAdapter.ItemViewHolder>() {

    fun updateList(newList: List<DocumentsItem>) {
        val diffCallback = DocumentsDiffCallback(itemList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        itemList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemDocumentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(
        private val binding: ItemDocumentBinding,
        private val onItemClick: (DocumentsItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentItem: DocumentsItem

        init {
            binding.root.setOnClickListener {
                onItemClick(currentItem)
            }
        }

        fun bind(item: DocumentsItem) {
            currentItem = item
            binding.type.text = item.type
            binding.date.text = item.date
            binding.status.text = item.status

            (binding.date.parent as? android.view.View)?.visibility =
                if (item.date.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE

            binding.status.visibility =
                if (item.status.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE

            if (item.status?.contains("готово", true) == true) {
                binding.status.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(binding.type, com.google.android.material.R.attr.colorPrimaryContainer)
                )
                binding.status.setTextColor(
                    MaterialColors.getColor(binding.type, com.google.android.material.R.attr.colorOnPrimaryContainer)
                )
            } else {
                binding.status.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(binding.type, com.google.android.material.R.attr.colorSurfaceVariant)
                )
                binding.status.setTextColor(
                    MaterialColors.getColor(binding.type, com.google.android.material.R.attr.colorOnSurfaceVariant)
                )
            }
        }
    }

    class DocumentsDiffCallback(
        private val oldList: List<DocumentsItem>,
        private val newList: List<DocumentsItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].number == newList[newItemPosition].number
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
