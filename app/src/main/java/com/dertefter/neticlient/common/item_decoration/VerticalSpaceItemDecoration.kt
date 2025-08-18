package com.dertefter.neticlient.common.item_decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration(
    private val verticalSpaceDimen: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val space = view.context.resources.getDimensionPixelSize(verticalSpaceDimen)
        val position = parent.getChildAdapterPosition(view)

        if (position != RecyclerView.NO_POSITION && position > 0) {
            outRect.top = space
        }
    }
}