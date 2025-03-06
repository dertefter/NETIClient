package com.dertefter.neticlient.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.messages.Message

class MessagesRecyclerViewAdapter(
    var messagesList: List<Message> = emptyList(),
    val fragment: MessagesTabFragment,
) : RecyclerView.Adapter<MessagesRecyclerViewAdapter.MessageViewHolder>() {

    fun setData(newList: List<Message>) {
        this.messagesList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = messagesList[position]
        holder.bind(item, fragment)
    }

    override fun getItemCount(): Int = messagesList.size

    override fun getItemViewType(position: Int): Int {
        return if (messagesList[position].is_new) R.layout.item_message_new else R.layout.item_message
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
}
