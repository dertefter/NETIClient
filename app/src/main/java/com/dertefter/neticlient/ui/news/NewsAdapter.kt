package com.dertefter.neticlient.ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.utils.Utils
import com.squareup.picasso.Picasso

class NewsAdapter(val newsFragment: NewsFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_NEWS = 0
        private const val TYPE_LOADING = 1
    }

    private var originalItems = mutableListOf<NewsItem>()
    private var filteredItems = mutableListOf<NewsItem>()
    private var selectedFilterType: String = "все"

    override fun getItemViewType(position: Int): Int {
        return if (position < filteredItems.size) TYPE_NEWS else TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_NEWS) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            NewsViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.skeleton_news, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder) {
            holder.bind(filteredItems[position], newsFragment)
        }
    }

    override fun getItemCount(): Int {
        return filteredItems.size + 1
    }

    fun addItems(newItems: List<NewsItem>) {
        originalItems.addAll(newItems)
        applyFilter()
        if (filteredItems.size == 1){
            newsFragment.loadNews()
        }
        notifyDataSetChanged()
    }

    fun setFilter(filterType: String) {
        if (selectedFilterType == filterType) return
        if (filterType == newsFragment.getString(R.string.filter_news_all)){
            selectedFilterType = "все"
        } else if (filterType == newsFragment.getString(R.string.filter_news_news)){
            selectedFilterType = "новости"
        } else if (filterType == newsFragment.getString(R.string.filter_news_obj)){
            selectedFilterType = "объявления"
        } else if (filterType == newsFragment.getString(R.string.filter_news_interv)){
            selectedFilterType = "интервью"
        } else if (filterType == newsFragment.getString(R.string.filter_news_photo)){
            selectedFilterType = "фото"
        }
        applyFilter()
        notifyDataSetChanged()
    }

    private fun applyFilter() {
        filteredItems = when (selectedFilterType.lowercase()) {
            "все" -> originalItems.toMutableList()
            else -> originalItems.filter { it.type.lowercase() == selectedFilterType.lowercase() }.toMutableList()
        }
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val tags = itemView.findViewById<TextView>(R.id.tags)
        private val type = itemView.findViewById<TextView>(R.id.type)
        private val imageView = itemView.findViewById<ImageView>(R.id.news_background)

        fun bind(item: NewsItem, newsFragment: NewsFragment) {
            title.text = item.title
            tags.text = item.tags
            type.text = item.type
            Picasso.get().load(item.imageUrl).into(imageView)
            itemView.setOnClickListener {
                newsFragment.openNewsDetail(item.id, item.imageUrl)
            }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}