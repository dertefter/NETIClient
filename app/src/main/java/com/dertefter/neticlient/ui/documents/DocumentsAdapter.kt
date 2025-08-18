package com.dertefter.neticlient.ui.documents

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class DocumentsAdapter(
    private var itemList: List<DocumentsItem> = emptyList(),
    private val onItemClick: (DocumentsItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var loading = false
    private val loadingItemCount = 7

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    fun updateList(newList: List<DocumentsItem>) {
        itemList = newList
        notifyDataSetChanged()
    }

    fun setLoading(v: Boolean) {
        if (loading != v) {
            loading = v
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (loading && position >= itemList.size) {
            TYPE_LOADING
        } else {
            TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)
            ItemViewHolder(view, onItemClick)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lesson, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = itemList[position]
            holder.bind(item)
        }

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

    override fun getItemCount(): Int {
        return itemList.size + if (loading) loadingItemCount else 0
    }

    class ItemViewHolder(
        itemView: View,
        private val onItemClick: (DocumentsItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val type: TextView = itemView.findViewById(R.id.type)
        private val status: TextView = itemView.findViewById(R.id.status)
        private val date: TextView = itemView.findViewById(R.id.date)
        private lateinit var currentItem: DocumentsItem

        init {
            itemView.setOnClickListener {
                onItemClick(currentItem)
            }
        }

        fun bind(item: DocumentsItem) {
            currentItem = item
            type.text = item.type
            date.text = item.date
            status.text = item.status

            (date.parent as View).visibility = if (date.text.isEmpty()) View.GONE else View.VISIBLE
            status.visibility = if (status.text.isEmpty()) View.GONE else View.VISIBLE

            if (item.status?.contains("готово", true) == true){
                status.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimaryContainer)
                )
                status.setTextColor(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnPrimaryContainer)
                )
            }else{
                status.backgroundTintList = ColorStateList.valueOf(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorSurfaceVariant)
                )
                status.setTextColor(
                    MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSurfaceVariant)
                )
            }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
