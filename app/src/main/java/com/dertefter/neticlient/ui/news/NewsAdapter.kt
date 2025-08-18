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
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.databinding.ItemNewsBinding
import com.dertefter.neticlient.databinding.ItemNewsLoadingBinding
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsAdapter(
    private val onItemClick: (NewsItem, Int) -> Unit,

) : ListAdapter<NewsItem, RecyclerView.ViewHolder>(NewsDiffCallback()) {

    companion object {
        private const val TYPE_NEWS = 0
        private const val TYPE_LOADING = 1
    }
    private  val loadingViewsCount: Int = 1
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

        return if (viewType == TYPE_NEWS) {
            val binding = ItemNewsBinding.inflate(inflater, parent, false)
            NewsViewHolder(binding, onItemClick)
        } else {
            val binding = ItemNewsLoadingBinding.inflate(inflater, parent, false)
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder && position < currentList.size) {
            holder.bind(currentList[position])

            val context = holder.itemView.context
            val cardView = holder.itemView as com.google.android.material.card.MaterialCardView
            val radiusMax = context.resources.getDimension(R.dimen.radius_max)
            val radiusMin = context.resources.getDimension(R.dimen.radius_micro)

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
    ) : RecyclerView.ViewHolder(binding.root)
    {

        private var color: Int = Color.GRAY

        private fun getDominantColor(bm: Bitmap): Int {
            return Palette.from(bm).generate().getDominantColor(Color.GRAY)
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
                            val bitmap = binding.newsBackground.drawable.toBitmap().scale(10, 10, false)
                            val newContext: Context = DynamicColors.wrapContextIfAvailable(
                                binding.root.context,
                                DynamicColorsOptions.Builder()
                                    .setContentBasedSource(bitmap)
                                    .build()
                            )

                            var cardBg = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.GRAY)
                            var titleColor = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorOnSurface, Color.GRAY)
                            var typeBg = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorPrimaryContainer, Color.GRAY)
                            var typeColor = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.GRAY)
                            var tagsColor = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)

                            color = typeBg

                            withContext(Dispatchers.Main) {
                                binding.root.setCardBackgroundColor(cardBg)
                                binding.title.setTextColor(titleColor)
                                binding.tags.setTextColor(tagsColor)
                                binding.type.setTextColor(typeColor)
                                binding.type.backgroundTintList = ColorStateList.valueOf(typeBg)
                            }
                        }
                    }

                    override fun onError(e: Exception?) {}
                })
        }
    }

    class LoadingViewHolder(binding: ItemNewsLoadingBinding) : RecyclerView.ViewHolder(binding.root)

    class NewsDiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsItem, newItem: NewsItem): Boolean {
            return oldItem == newItem
        }
    }
}

