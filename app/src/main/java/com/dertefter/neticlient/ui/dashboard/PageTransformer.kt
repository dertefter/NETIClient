package com.dertefter.neticlient.ui.dashboard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.utils.Utils

class PageTransformer(context: Context) : ViewPager2.PageTransformer {

    private val offsetPx = Utils.dpToPx(context, 0f)
    private val pageMarginPx = Utils.dpToPx(context, 48f)

    override fun transformPage(page: View, position: Float) {
        val viewPager = requireViewPager(page)
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView ?: return
        val holder = recyclerView.findContainingViewHolder(page) ?: return
        val adapterPosition = holder.adapterPosition
        val itemCount = viewPager.adapter?.itemCount ?: 0
        val isLastPage = adapterPosition == itemCount - 1

        val offset = position * -(2 * offsetPx + pageMarginPx)
        val totalMargin = offsetPx + pageMarginPx
        val isRTL = ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL

        page.alpha = when {
            position <= -1 -> 0f
            position in -1f..0f -> 0.5f + 0.5f * (1 + position)
            position in 0f..1f -> 1f - 0.5f * position
            position >= 1 -> 0.5f
            else -> 1f
        }

        if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                if (isLastPage) {
                    if (isRTL) {
                        marginStart = 0
                        marginEnd = totalMargin
                    } else {
                        marginStart = totalMargin
                        marginEnd = 0
                    }
                } else {
                    marginStart = totalMargin
                    marginEnd = totalMargin
                }
            }

            page.translationX = if (isRTL) {
                -offset
            } else {
                offset
            }
        } else {
            page.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 0
                bottomMargin = if (isLastPage) 0 else totalMargin
            }

            page.translationY = offset
        }
    }

    private fun requireViewPager(page: View): ViewPager2 {
        val parent = page.parent
        val parentParent = parent?.parent
        if (parent is RecyclerView && parentParent is ViewPager2) {
            return parentParent
        }
        throw IllegalStateException(
            "Expected the page view to be managed by a ViewPager2 instance."
        )
    }
}