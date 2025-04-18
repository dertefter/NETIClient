package com.dertefter.neticlient.ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.common.utils.Utils
import com.squareup.picasso.Picasso

class NewsAdapter(val newsFragment: NewsFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_NEWS = 0
        private const val TYPE_LOADING = 1
    }

    private var items = mutableListOf<NewsItem>()

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) TYPE_NEWS else TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_NEWS) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            NewsViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            newsFragment.loadNews()
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder) {
            holder.bind(items[position], newsFragment)
        }
    }

    override fun getItemCount(): Int {
        return items.size + 20
    }

    fun addItems(newItems: List<NewsItem>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun clearItems() {
        items.clear()
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