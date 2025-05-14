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

class MessagesRecyclerViewAdapter(
    val fragment: MessagesTabFragment,
) : ListAdapter<Message, MessagesRecyclerViewAdapter.MessageViewHolder>(DiffCallback()) {

    fun setData(newList: List<Message>) {
        submitList(newList)
    }

    fun setLoading() {
        val loadingList = List(30) {
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
                fragment.openMessageDetail(item.id)
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
