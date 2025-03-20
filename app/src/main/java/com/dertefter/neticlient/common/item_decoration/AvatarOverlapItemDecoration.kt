package com.dertefter.neticlient.common.item_decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class AvatarOverlapItemDecoration(val context: Context,
                                  @DimenRes val overlap: Int,
    ) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position != 0) {
            outRect.left = context.resources.getDimensionPixelSize(overlap) * (-1)
        }
    }
}