package com.dertefter.neticlient.ui.news.news_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.ui.fullscreen_image_dialog.FullscreenImageDialog
import com.squareup.picasso.Picasso

class ImagesCarouselAdapter(val fragment: NewsDetailFragment) : RecyclerView.Adapter<ImagesCarouselAdapter.ImageViewHolder>() {
    private val items: MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_carusel, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position], fragment)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageUrl: String?, fragment: NewsDetailFragment) {
            Picasso.get().load(imageUrl).into(imageView)
            itemView.setOnClickListener {
                val dialog = FullscreenImageDialog().apply {arguments = Bundle().apply { putString("image_url", imageUrl) } }
                dialog.show(fragment.childFragmentManager, "FullscreenImageDialog")
            }
        }

    }
}