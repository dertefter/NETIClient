package com.dertefter.neticlient.common

import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.dertefter.neticlient.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar

class AppBarEdgeToEdge(
    private val appBar: AppBarLayout
) : AppBarLayout.OnOffsetChangedListener, ViewTreeObserver.OnGlobalLayoutListener {

    private val childInfo = mutableListOf<ChildInfo>()
    private var statusBarInset = 0
    private var targetBottomPadding = 0

    init {
        appBar.setLiftable(false)
        setupInsets()
        appBar.addOnOffsetChangedListener(this)
        // Добавляем слушатель изменения макета
        appBar.viewTreeObserver.addOnGlobalLayoutListener(this)
        targetBottomPadding = appBar.context.resources
            .getDimensionPixelSize(R.dimen.margin_min)
    }

    // ✅ Метод для очистки слушателей, чтобы избежать утечек памяти
    fun destroy() {
        appBar.removeOnOffsetChangedListener(this)
        appBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    // ✅ Этот метод будет вызываться каждый раз, когда макет будет перерисован
    override fun onGlobalLayout() {
        updateChildInfo()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarInset = systemBars.top

            view.updatePadding(
                top = systemBars.top,
            )

            // Первоначальный вызов остается на всякий случай
            updateChildInfo()
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateChildInfo() {
        // Проверяем, изменилось ли что-то, чтобы не создавать объекты без надобности
        if (childInfo.size == appBar.childCount && childInfo.all { it.height > 0 }) {
            // Если количество дочерних элементов совпадает и у всех уже есть высота,
            // можно предположить, что ничего не изменилось. Для более строгой проверки можно сравнивать высоты.
            var hasChanged = false
            for (i in 0 until appBar.childCount){
                if (childInfo[i].height != appBar.getChildAt(i).height || childInfo[i].top != appBar.getChildAt(i).top - statusBarInset){
                    hasChanged = true
                    break
                }
            }
            if (!hasChanged) return
        }

        childInfo.clear()
        for (i in 0 until appBar.childCount) {
            val child = appBar.getChildAt(i)
            childInfo.add(ChildInfo(child.top - statusBarInset, child.height))
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalRange = appBarLayout.totalScrollRange
        if (totalRange == 0) return

        val scroll = -verticalOffset
        val progress = scroll.toFloat() / totalRange

        // 💡 Эту логику можно упростить. Она очень хрупкая.
        // Лучше дать контейнеру для заголовка ID и искать его по ID,
        // а не полагаться на иерархию и типы View.
        val lastChild = appBar.getChildAt(appBar.childCount - 1)
        if (lastChild !is MaterialToolbar) {
            val isToolbarContainer = lastChild is FrameLayout && lastChild.getChildAt(0) is MaterialToolbar
            if (!isToolbarContainer) {
                val newBottomPadding = (targetBottomPadding * progress).toInt()
                appBar.updatePadding(bottom = newBottomPadding)
            }
        }

        if (childInfo.size != appBar.childCount) {
            // Если информация устарела, принудительно обновляем
            updateChildInfo()
        }

        for (i in 0 until appBar.childCount) {
            val view = appBar.getChildAt(i)
            val (top, height) = childInfo.getOrNull(i) ?: continue
            if (height == 0) continue

            val hidden = (scroll - top).coerceIn(0, height)
            val alpha = 1f - (hidden.toFloat() / height * 1.5f)
            val scale = 1f - (hidden.toFloat() / height) / 10

            view.alpha = alpha.coerceIn(0f, 1f)
            view.scaleX = scale.coerceIn(0f, 1f)
            view.scaleY = scale.coerceIn(0f, 1f)
        }
        appBar.isLifted = scroll == totalRange
    }

    private data class ChildInfo(val top: Int, val height: Int)
}