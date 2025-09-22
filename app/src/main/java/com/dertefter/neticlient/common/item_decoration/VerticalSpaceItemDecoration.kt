package com.dertefter.neticlient.common.item_decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.CornerSize

class VerticalSpaceItemDecoration(
    private val radiusMaxDimen: Int,
    private val radiusMinDimen: Int,
    private val verticalSpaceDimen: Int,
    private val skipLeft: Boolean = false
) : RecyclerView.ItemDecoration() {

    private val EPS = 0.75f

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val verticalSpace = view.context.resources.getDimension(verticalSpaceDimen)
        val position = parent.getChildAdapterPosition(view)
        val maxIndexPosition = state.itemCount - 1
        if (position == RecyclerView.NO_POSITION) return

        when (position) {
            0 -> outRect.bottom = verticalSpace.toInt() / 2
            maxIndexPosition -> outRect.top = verticalSpace.toInt() / 2
            else -> {
                outRect.top = verticalSpace.toInt() / 2
                outRect.bottom = verticalSpace.toInt() / 2
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val radiusMax = parent.context.resources.getDimension(radiusMaxDimen)
        val radiusMin = parent.context.resources.getDimension(radiusMinDimen)
        val itemCount = state.itemCount

        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i) as? MaterialCardView ?: continue
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) continue

            val desired = buildDesiredModel(position, itemCount, radiusMax, radiusMin, skipLeft)

            if (!hasSameCornerRadii(view, desired)) {
                view.shapeAppearanceModel = desired
            }
        }
    }

    private fun buildDesiredModel(
        position: Int,
        itemCount: Int,
        radiusMax: Float,
        radiusMin: Float,
        skipLeft: Boolean
    ): ShapeAppearanceModel {
        val isFirst = position == 0
        val isLast = position == itemCount - 1

        val tl = when {
            skipLeft -> 0f
            isFirst -> radiusMax
            else -> radiusMin
        }
        val tr = if (isFirst) radiusMax else radiusMin
        val bl = when {
            skipLeft -> 0f
            isLast -> radiusMax
            else -> radiusMin
        }
        val br = if (isLast) radiusMax else radiusMin

        return ShapeAppearanceModel().toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, tl)
            .setTopRightCorner(CornerFamily.ROUNDED, tr)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bl)
            .setBottomRightCorner(CornerFamily.ROUNDED, br)
            .build()
    }

    private fun CornerSize.toPx(bounds: RectF): Float = getCornerSize(bounds)

    private fun hasSameCornerRadii(
        card: MaterialCardView,
        desired: ShapeAppearanceModel
    ): Boolean {
        // Важно сравнивать радиусы в пикселях с учётом размеров вью
        val bounds = RectF(0f, 0f, card.width.toFloat(), card.height.toFloat())
        val current = card.shapeAppearanceModel

        val cTL = current.topLeftCornerSize.toPx(bounds)
        val cTR = current.topRightCornerSize.toPx(bounds)
        val cBL = current.bottomLeftCornerSize.toPx(bounds)
        val cBR = current.bottomRightCornerSize.toPx(bounds)

        val dTL = desired.topLeftCornerSize.toPx(bounds)
        val dTR = desired.topRightCornerSize.toPx(bounds)
        val dBL = desired.bottomLeftCornerSize.toPx(bounds)
        val dBR = desired.bottomRightCornerSize.toPx(bounds)

        return approxEq(cTL, dTL) &&
                approxEq(cTR, dTR) &&
                approxEq(cBL, dBL) &&
                approxEq(cBR, dBR)
    }

    private fun approxEq(a: Float, b: Float): Boolean = kotlin.math.abs(a - b) <= EPS
}
