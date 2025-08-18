package com.dertefter.neticlient.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.messages.Message
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class MessagesRecyclerViewAdapter(
    val fragment: MessagesTabFragment,
) : ListAdapter<Message, MessagesRecyclerViewAdapter.MessageViewHolder>(DiffCallback()) {

    fun setData(newList: List<Message>) {
        val sortedList = newList.sortedByDescending { it.is_new }
        submitList(sortedList)
    }

    fun setLoading() {
        val loadingList = List(15) {
            Message("", "", "", "", false, "")
        }
        submitList(loadingList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, fragment)

        val context = holder.itemView.context
        val cardView = holder.itemView as com.google.android.material.card.MaterialCardView
        val radiusMax = context.resources.getDimension(R.dimen.radius_max)
        val radiusMin = context.resources.getDimension(R.dimen.radius_micro)

        // Логика скругления углов остаётся прежней и будет работать корректно,
        // так как она зависит от позиции элемента в уже отсортированном списке.
        val shapeModel = when (position) {
            0 -> ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radiusMax)
                .setTopRightCorner(CornerFamily.ROUNDED, radiusMax)
                .setBottomLeftCorner(CornerFamily.ROUNDED, radiusMin)
                .setBottomRightCorner(CornerFamily.ROUNDED, radiusMin)
                .build()

            itemCount - 1 -> ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radiusMin)
                .setTopRightCorner(CornerFamily.ROUNDED, radiusMin)
                .setBottomLeftCorner(CornerFamily.ROUNDED, radiusMax)
                .setBottomRightCorner(CornerFamily.ROUNDED, radiusMax)
                .build()

            else -> ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radiusMin)
                .build()
        }

        cardView.shapeAppearanceModel = shapeModel

    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).is_new) R.layout.item_message_new else R.layout.item_message
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sender_name: TextView = itemView.findViewById(R.id.sender_name)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val text: TextView = itemView.findViewById(R.id.text)

        fun bind(item: Message, fragment: MessagesTabFragment) {
            sender_name.text = item.send_by
            title.text = item.title
            text.text = item.text
            itemView.setOnClickListener {
                if (item.id.isNotEmpty()){
                    fragment.openMessageDetail(item.id)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}