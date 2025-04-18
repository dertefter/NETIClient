package com.dertefter.neticlient.ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.news.PromoItem
import com.squareup.picasso.Picasso

class PromoAdapter(private val items: List<PromoItem>, private val onClick: (PromoItem) -> Unit) :
    RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    class PromoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.promo_title)
        private val image: ImageView = view.findViewById(R.id.promo_bg)

        fun bind(item: PromoItem, onClick: (PromoItem) -> Unit) {
            title.text = item.title
            Picasso.get().load(item.imageUrl).into(image)
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_promo_carusel, parent, false)
        return PromoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size
}