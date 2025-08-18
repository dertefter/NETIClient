package com.dertefter.neticlient.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.graphics.createBitmap
import com.google.android.material.color.MaterialColors

class ShimmerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.TRANSPARENT)
    }

    private var alphaValue = 128
    private var animator: ValueAnimator? = null

    init {
        setWillNotDraw(false)
        startShimmerAnimation()
    }

    private fun startShimmerAnimation() {
        animator = ValueAnimator.ofInt(80, 160).apply {
            duration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                alphaValue = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        shimmerPaint.alpha = alphaValue

        children.forEach { child ->
            if (child.visibility != View.VISIBLE || child.width == 0 || child.height == 0) return@forEach

            val childBitmap = createBitmap(child.width, child.height)
            val childCanvas = Canvas(childBitmap)
            child.draw(childCanvas)

            val maskBitmap = createBitmap(child.width, child.height)
            val maskCanvas = Canvas(maskBitmap)

            // Рисуем серую заливку в маску
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = shimmerPaint.color
                alpha = shimmerPaint.alpha
            }
            maskCanvas.drawRect(0f, 0f, child.width.toFloat(), child.height.toFloat(), fillPaint)

            // Маска DST_IN — сохраняем только перекрытие с формой вью
            val layerId = canvas.saveLayer(
                child.left.toFloat(),
                child.top.toFloat(),
                (child.left + child.width).toFloat(),
                (child.top + child.height).toFloat(),
                null
            )

            canvas.drawBitmap(maskBitmap, child.left.toFloat(), child.top.toFloat(), null)

            val maskPaint = Paint().apply {
                isAntiAlias = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            canvas.drawBitmap(childBitmap, child.left.toFloat(), child.top.toFloat(), maskPaint)
            canvas.restoreToCount(layerId)

            childBitmap.recycle()
            maskBitmap.recycle()
        }
    }
}