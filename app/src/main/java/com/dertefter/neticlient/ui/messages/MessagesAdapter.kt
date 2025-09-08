package com.dertefter.neticlient.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.ItemMessageBinding
import com.dertefter.neticore.features.inbox.model.Message
import com.google.android.material.color.MaterialColors
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessagesAdapter(
    private val onItemClick: (Message) -> Unit
) : ListAdapter<Message, MessagesAdapter.MessageViewHolder>(DiffCallback) {


    private var originalList: List<Message> = emptyList()
    private var currentFilter: MessageFilter = MessageFilter.ALL

    fun updateData(newList: List<Message>) {
        originalList = newList
        submitList(applyFilter())

    }

    fun setFilter(filter: MessageFilter) {
        currentFilter = filter
        submitList(applyFilter())
    }

    fun clearFilter() {
        currentFilter = MessageFilter.ALL
        submitList(applyFilter())
    }


    private fun applyFilter(): List<Message> {
        val filteredList = when (currentFilter) {
            MessageFilter.ALL -> {originalList.filter { it.isDeleted == 0}}
            MessageFilter.UNREAD -> {originalList.filter { it.isRead == 0 && it.isDeleted == 0}}
            MessageFilter.TEACHER -> {originalList.filter { it.senderType == 2 && it.isDeleted == 0 }}
            MessageFilter.DECAN -> {originalList.filter { it.senderType == 1 && it.isDeleted == 0 }}
            MessageFilter.TUTOR -> {originalList.filter { it.senderType == 4 && it.isDeleted == 0}}
            MessageFilter.SLUZHB -> {originalList.filter { it.senderType == 3 && it.isDeleted == 0 }}
            MessageFilter.OTHER -> {originalList.filter { it.senderType == 0 && it.isDeleted == 0 }}
            MessageFilter.TRASH -> {originalList.filter { it.isDeleted == 1 }}
        }
        return filteredList.sortedByDescending { it.getLocalDateTime() }
    }


    object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
            oldItem == newItem
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val cardBgColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurfaceContainer)
        val cardBgUnreadColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorPrimaryContainer)
        val titleColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurfaceVariant)
        val titleUnreadColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimaryContainer)

        fun formatMessageDate(dateTime: LocalDateTime?): String {
            if (dateTime == null) return ""
            val now = LocalDateTime.now()

            return when {
                dateTime.toLocalDate() == now.toLocalDate() -> {
                    dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                }
                dateTime.year == now.year -> {
                    dateTime.format(DateTimeFormatter.ofPattern("d MMM"))
                }
                else -> {
                    dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                }
            }
        }

        fun bind(item: Message) {
            binding.title.text = item.title
            binding.text.text = HtmlCompat.fromHtml(item.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.senderName.text = item.fioAuthor
            if (item.isRead != 0) {
                binding.date.setTextColor(titleColor)
                binding.title.setTextColor(titleColor)
                binding.text.setTextColor(titleColor)
                binding.senderName.setTextColor(titleColor)
                binding.root.setCardBackgroundColor(cardBgColor)

            } else {
                binding.date.setTextColor(titleUnreadColor)
                binding.title.setTextColor(titleUnreadColor)
                binding.text.setTextColor(titleUnreadColor)
                binding.senderName.setTextColor(titleUnreadColor)
                binding.root.setCardBackgroundColor(cardBgUnreadColor)
            }

            binding.date.text = formatMessageDate(item.getLocalDateTime())

            if (!item.portraitUrl.isNullOrEmpty()) {
                binding.profilePic.isGone = false
                Picasso.get().load(item.portraitUrl).into(binding.profilePic)
            } else {
                binding.profilePic.isGone = true
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}