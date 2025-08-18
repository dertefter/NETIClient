package com.dertefter.neticlient.common

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import com.dertefter.neticlient.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar

class AppBarEdgeToEdge(
    private val appBar: AppBarLayout
) : AppBarLayout.OnOffsetChangedListener {

    private val childInfo = mutableListOf<ChildInfo>()
    private var statusBarInset = 0
    private var targetBottomPadding = 0 // Добавляем переменную для целевого отступа

    init {
        appBar.setLiftable(false)
        setupInsets()
        appBar.addOnOffsetChangedListener(this)

        // Получаем значение из ресурсов
        targetBottomPadding = appBar.context.resources
            .getDimensionPixelSize(R.dimen.margin_min)
    }


    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarInset = systemBars.top

            view.updatePadding(
                top = systemBars.top,
            )

            appBar.post { updateChildInfo() }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateChildInfo() {
        childInfo.clear()
        for (i in 0 until appBar.childCount) {
            val child = appBar.getChildAt(i)
            // Корректируем позицию с учетом статус-бара
            childInfo.add(ChildInfo(child.top - statusBarInset, child.height))
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalRange = appBarLayout.totalScrollRange
        if (totalRange == 0) return

        val scroll = -verticalOffset
        val progress = scroll.toFloat() / totalRange // 0f..1f

        if (appBar.getChildAt(appBar.childCount - 1) !is MaterialToolbar){
            val lastChild = appBar.getChildAt(appBar.childCount - 1)
            if (lastChild is FrameLayout){
                if (lastChild.getChildAt(0) !is MaterialToolbar){
                    val newBottomPadding = (targetBottomPadding * progress).toInt()
                    appBar.updatePadding(bottom = newBottomPadding)
                }
                }

        }



        for (i in 0 until appBar.childCount) {
            val view = appBar.getChildAt(i)
            val (top, height) = childInfo.getOrNull(i) ?: continue
            if (height == 0) continue

            val hidden = (scroll - top).coerceIn(0, height)
            val alpha = 1f - (hidden.toFloat() / height)
            val scale =  1f - (hidden.toFloat() / height) / 10

            view.alpha = alpha.coerceIn(0f, 1f)
            view.scaleX = scale.coerceIn(0f, 1f)
            view.scaleY = scale.coerceIn(0f, 1f)
        }
        appBar.isLifted = scroll == totalRange
    }

    private data class ChildInfo(val top: Int, val height: Int)
}