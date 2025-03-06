package com.dertefter.neticlient.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val spanCount: Int, private val s: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // Позиция элемента
        val column = position % spanCount // Определение колонки
        val spacing = s.toPx(view.context) // Преобразование dp в пиксели
        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount
        if (position >= spanCount) {
            outRect.top = spacing // Отступ сверху для всех, кроме первого ряда
        }
    }

    fun Int.toPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}