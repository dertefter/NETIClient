package com.dertefter.neticlient.ui.messages.dispace_messages

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.dispace.messages.Companion
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.Time
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import java.time.LocalTime

class DispaceMessagesRecyclerViewAdapter(
    var messagesList: List<Companion> = emptyList(),
) : RecyclerView.Adapter<DispaceMessagesRecyclerViewAdapter.MessageViewHolder>() {

    fun setData(newList: List<Companion>) {
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
        holder.bind(item)
    }

    override fun getItemCount(): Int = messagesList.size

    override fun getItemViewType(position: Int): Int {
        return if (messagesList[position].isNew == "1") R.layout.item_message_dispace_new else R.layout.item_message_dispace
    }


    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sender_name: TextView = itemView.findViewById(R.id.sender_name)
        private val avatar: ShapeableImageView = itemView.findViewById(R.id.profilePic)
        private val text: TextView = itemView.findViewById(R.id.text)

        fun bind(item: Companion) {
            sender_name.text = "${item.surname} ${item.name} ${item.patronymic}"
            Picasso.get().load("https://dispace.edu.nstu.ru/files/images/photos/b_IMG_"+item.photo).resize(200,200).centerCrop().into(avatar)
            text.text = item.lastMsg
        }
    }
}
