package com.dertefter.neticlient.common.item_decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class GridSpacingItemDecoration(
    context: Context,
    private val spanCount: Int,
    @DimenRes horizontalSpacingResId: Int,
    @DimenRes verticalSpacingResId: Int = horizontalSpacingResId
) : ItemDecoration() {
    private val horizontalSpacing = context.resources.getDimensionPixelSize(horizontalSpacingResId)
    private val verticalSpacing = context.resources.getDimensionPixelSize(verticalSpacingResId)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        val column = position % spanCount
        outRect.left = horizontalSpacing - column * horizontalSpacing / spanCount
        outRect.right = (column + 1) * horizontalSpacing / spanCount

        if (position >= spanCount) {
            outRect.top = verticalSpacing
        }
    }
}

