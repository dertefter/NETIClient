package com.dertefter.neticlient.ui.news

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.palette.graphics.Palette
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.databinding.ItemPromoCaruselBinding
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PromoAdapter(
    private val onClick: (PromoItem) -> Unit
) : ListAdapter<PromoItem, PromoAdapter.PromoViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<PromoItem>() {
            override fun areItemsTheSame(oldItem: PromoItem, newItem: PromoItem): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: PromoItem, newItem: PromoItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    class PromoViewHolder(
        private val binding: ItemPromoCaruselBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PromoItem, onClick: (PromoItem) -> Unit) = with(binding) {
            promoTitle.text = item.title

            val target = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    binding.bgImage.setImageBitmap(bitmap)
                    CoroutineScope(Dispatchers.IO).launch {
                        val paletteSwatch = Palette.from(bitmap).generate().vibrantSwatch
                        val textColor = paletteSwatch?.titleTextColor
                        val backgroundColor = paletteSwatch?.rgb
                        if (textColor != null && backgroundColor != null) {
                            withContext(Dispatchers.Main) {
                                promoTitle.setTextColor(textColor)
                                promoTitle.backgroundTintList = ColorStateList.valueOf(backgroundColor)
                                bgGradient.imageTintList = ColorStateList.valueOf(textColor)
                            }
                        }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            }

            binding.bgImage.tag = target
            Picasso.get()
                .load(item.imageUrl)
                .resize(1024, 1024)
                .onlyScaleDown()
                .centerInside()
                .into(target)

            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromoCaruselBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PromoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClick)
    }
}