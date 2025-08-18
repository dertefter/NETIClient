package com.dertefter.neticlient.common

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withSave
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapes
import kotlin.math.max
import kotlin.math.min
import com.dertefter.neticlient.R
import androidx.core.content.withStyledAttributes

class AnotherShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(
            this@AnotherShapeView,
            com.google.android.material.R.attr.colorSurfaceContainerLow,
            Color.GRAY
        )
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val scaleMatrix = Matrix()
    private val bitmapMatrix = Matrix()

    private var currentBitmap: Bitmap? = null
    private val shape: RoundedPolygon = getRandomShape()

    private val boundsRect = RectF()
    private val tempRect = RectF()

    var foregroundDrawable: Drawable? = null
        set(value) {
            field = value
            invalidate()
        }

    init {


        // foreground можно задать в XML как app:rippleForeground
        context.withStyledAttributes(attrs, R.styleable.AnotherShapeView) {
            foregroundDrawable = getDrawable(R.styleable.AnotherShapeView_rippleForeground)
        }

        foregroundDrawable?.callback = this

        isClickable = true
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        if (bitmap !== currentBitmap) {
            currentBitmap = bitmap
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        foregroundDrawable?.let { fg ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    fg.state = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
                    fg.setHotspot(event.x, event.y)
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    fg.state = intArrayOf()
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Формируем path из формы
        path.reset()
        shape.toPath(path)
        path.computeBounds(boundsRect, true)

        val size = min(width, height).toFloat()
        val scale = if (boundsRect.width() > 0 && boundsRect.height() > 0) {
            min(size / boundsRect.width(), size / boundsRect.height())
        } else 1f

        scaleMatrix.setScale(scale, scale)
        path.transform(scaleMatrix)
        path.computeBounds(tempRect, true)
        path.offset(
            (width - tempRect.width()) / 2f - tempRect.left,
            (height - tempRect.height()) / 2f - tempRect.top
        )

        // Рисуем содержимое
        canvas.withSave {
            clipPath(path)
            currentBitmap?.let { bmp ->
                val pathBounds = RectF()
                path.computeBounds(pathBounds, true)

                bitmapMatrix.reset()
                val scaleX = pathBounds.width() / bmp.width
                val scaleY = pathBounds.height() / bmp.height
                val scaleBmp = max(scaleX, scaleY)
                bitmapMatrix.postScale(scaleBmp, scaleBmp)
                bitmapMatrix.postTranslate(
                    pathBounds.centerX() - (bmp.width * scaleBmp) / 2f,
                    pathBounds.centerY() - (bmp.height * scaleBmp) / 2f
                )
                canvas.drawBitmap(bmp, bitmapMatrix, null)
            } ?: run {
                drawPath(path, paint)
            }

            foregroundDrawable?.let { fg ->
                fg.bounds = Rect(0, 0, width, height)
                fg.draw(canvas)
            }
        }
    }

    companion object {
        private val allShapes by lazy {
            listOf(
                MaterialShapes.BUN, MaterialShapes.ARCH, MaterialShapes.CLOVER_8, MaterialShapes.CLOVER_4,
                MaterialShapes.SLANTED_SQUARE, MaterialShapes.FAN, MaterialShapes.FLOWER, MaterialShapes.PILL,
                MaterialShapes.PUFFY, MaterialShapes.ARROW, MaterialShapes.CLAM_SHELL, MaterialShapes.COOKIE_6,
                MaterialShapes.PIXEL_CIRCLE, MaterialShapes.PIXEL_TRIANGLE, MaterialShapes.TRIANGLE, MaterialShapes.CIRCLE,
                MaterialShapes.COOKIE_12, MaterialShapes.PENTAGON, MaterialShapes.SEMI_CIRCLE, MaterialShapes.VERY_SUNNY
            )
        }

        fun getRandomShape(): RoundedPolygon = allShapes.random()
    }
}
