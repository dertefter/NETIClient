package com.dertefter.neticlient.ui.news

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.databinding.ItemNewsBinding
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsAdapter(
    private val onItemClick: (NewsItem, Int) -> Unit,
    val loadingViewsCount: Int = 6
) : ListAdapter<NewsItem, RecyclerView.ViewHolder>(NewsDiffCallback()) {

    companion object {
        private const val TYPE_NEWS = 0
        private const val TYPE_LOADING = 1
    }

    var filter: String? = null

    private var allItems: List<NewsItem> = emptyList()
    private var filteredList: List<NewsItem> = emptyList()

    fun filterList() {
        filteredList = if (filter == null) {
            allItems
        } else {
            allItems.filter { it.type.equals(filter, ignoreCase = true) }
        }
        submitList(filteredList.toList())
    }

    fun updateFilter(s: String?) {
        filter = s
        filterList()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) TYPE_NEWS else TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNewsBinding.inflate(inflater, parent, false)
        return if (viewType == TYPE_NEWS) {
            NewsViewHolder(binding, onItemClick)
        } else {
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder && position < currentList.size) {
            holder.bind(currentList[position])
        }
    }

    override fun getItemCount(): Int {
        return currentList.size + loadingViewsCount
    }

    fun updateItems(newItems: List<NewsItem>) {
        allItems = ArrayList(newItems)
        filterList()
    }

    class NewsViewHolder(
        private val binding: ItemNewsBinding,
        private val onItemClick: (NewsItem, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var color: Int = Color.GRAY

        private fun getDominantColor(bm: Bitmap): Int {
            return Palette.from(bm).generate().getVibrantColor(Color.GRAY)
        }

        fun bind(item: NewsItem) {
            binding.title.text = item.title
            binding.tags.text = item.tags
            binding.type.text = item.type
            binding.type.visibility = View.VISIBLE

            binding.root.setOnClickListener {
                onItemClick(item, color)
            }

            if (item.imageUrl.isNullOrEmpty()) {
                binding.newsBackground.visibility = View.GONE
                return
            } else {
                binding.newsBackground.visibility = View.VISIBLE
            }

            Picasso.get()
                .load(item.imageUrl)
                .into(binding.newsBackground, object : Callback {
                    override fun onSuccess() {
                        CoroutineScope(Dispatchers.IO).launch {
                            val bitmap = binding.newsBackground.drawable.toBitmap()
                            val compressed = bitmap.scale(10, 6, false)
                            color = getDominantColor(compressed)

                            val c: Context = DynamicColors.wrapContextIfAvailable(
                                binding.root.context,
                                DynamicColorsOptions.Builder()
                                    .setContentBasedSource(color)
                                    .build()
                            )

                            withContext(Dispatchers.Main) {
                                binding.root.setCardBackgroundColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorSurfaceContainer, Color.GRAY))
                                binding.title.setTextColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorOnSurface, Color.GRAY))
                                binding.tags.setTextColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY))
                                binding.type.setTextColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.GRAY))
                                binding.type.backgroundTintList = ColorStateList.valueOf(MaterialColors.getColor(c, com.google.android.material.R.attr.colorPrimaryContainer, Color.GRAY))
                            }
                        }
                    }

                    override fun onError(e: Exception?) {}
                })
        }
    }

    class LoadingViewHolder(binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root)

    class NewsDiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem == newItem
        }
    }
}

