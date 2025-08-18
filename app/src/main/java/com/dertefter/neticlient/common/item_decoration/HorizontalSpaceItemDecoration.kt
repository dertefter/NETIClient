package com.dertefter.neticlient.common.item_decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(
    private val horizontalSpaceDimen: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val space = view.context.resources.getDimensionPixelSize(horizontalSpaceDimen)
        val position = parent.getChildAdapterPosition(view)

        if (position != RecyclerView.NO_POSITION && position > 0) {
            outRect.left = space
        }
    }
}