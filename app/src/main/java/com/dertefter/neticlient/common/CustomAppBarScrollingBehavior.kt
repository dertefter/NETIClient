package com.dertefter.neticlient.common

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

class CustomAppBarScrollingBehavior @JvmOverloads constructor(
    context: Context? = null,
    attrs: AttributeSet? = null
) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        val result = super.onLayoutChild(parent, child, layoutDirection)

        val appBar = parent.getDependencies(child).find { it is AppBarLayout } as? AppBarLayout
        appBar?.let {
            val params = child.layoutParams as CoordinatorLayout.LayoutParams
            params.topMargin = -it.height
            child.updatePadding(top = it.height)
        }

        return result
    }

}
