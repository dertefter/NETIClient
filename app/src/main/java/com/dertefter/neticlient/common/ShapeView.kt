package com.dertefter.neticlient.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withSave
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapes
import kotlinx.coroutines.*
import kotlin.math.min
import kotlin.random.Random

class ShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(
            this@ShapeView,
            com.google.android.material.R.attr.colorSurfaceContainerHigh,
            Color.GRAY
        )
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val scaleMatrix = Matrix()
    private val bitmapMatrix = Matrix()
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentBitmap: Bitmap? = null
    private var nextBitmap: Bitmap? = null

    private var crossfadeProgress = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var progress = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private var crossfadeAnimator: ObjectAnimator? = null
    private var progressAnimator: ObjectAnimator? = null

    private val drawableResIds = mutableListOf<Int>()
    private var currentShape: RoundedPolygon = getRandomShape()
    private var nextShape: RoundedPolygon = getRandomShape()
    private var morph: Morph? = null

    // Cache for bounds calculation
    private val boundsRect = RectF()
    private val tempRect = RectF()
    private val bitmapRect = RectF()

    fun setDrawableResIds(ids: List<Int>) {
        drawableResIds.clear()
        drawableResIds.addAll(ids)
        resetBitmaps()
        if (ids.isNotEmpty()) {
            loadImageAsync(true)
            if (ids.size > 1) loadImageAsync(false)
        }
    }

    private fun loadImageAsync(isCurrent: Boolean) {
        if (width <= 0 || height <= 0 || drawableResIds.isEmpty()) {
            post { loadImageAsync(isCurrent) }
            return
        }

        val index = Random.nextInt(drawableResIds.size)
        scope.launch(Dispatchers.IO) {
            val drawable = context.getDrawable(drawableResIds[index]) ?: return@launch
            val bitmap = try {
                // Create a square bitmap based on the smallest dimension
                val size = min(width, height)
                Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                    Canvas(this).apply {
                        // Calculate scale to fit the drawable into the square while maintaining aspect ratio
                        val drawableWidth = drawable.intrinsicWidth
                        val drawableHeight = drawable.intrinsicHeight

                        if (drawableWidth > 0 && drawableHeight > 0) {
                            val scale = min(
                                size.toFloat() / drawableWidth,
                                size.toFloat() / drawableHeight
                            )

                            val scaledWidth = drawableWidth * scale
                            val scaledHeight = drawableHeight * scale

                            // Center the drawable in the square
                            val left = (size - scaledWidth) / 2f
                            val top = (size - scaledHeight) / 2f

                            save()
                            translate(left, top)
                            scale(scale, scale)
                            drawable.setBounds(0, 0, drawableWidth, drawableHeight)
                            drawable.draw(this)
                            restore()
                        }
                    }
                }
            } catch (e: Exception) {
                null
            }

            bitmap?.let {
                withContext(Dispatchers.Main) {
                    if (isCurrent) {
                        currentBitmap?.recycle()
                        currentBitmap = it
                    } else {
                        nextBitmap?.recycle()
                        nextBitmap = it
                    }
                    invalidate()
                }
            }
        }
    }

    private fun scheduleNextImageSwitch() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ startCrossfadeAnimation() }, 3000)
    }

    private fun startCrossfadeAnimation() {
        crossfadeAnimator?.cancel()
        progressAnimator?.cancel()
        crossfadeProgress = 0f
        progress = 0f

        nextShape = getRandomShape()
        morph = Morph(currentShape, nextShape)

        progressAnimator = ObjectAnimator.ofFloat(this, "progress", 0f, 1f).apply {
            duration = 500
            interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
            start()
        }

        crossfadeAnimator = ObjectAnimator.ofFloat(this, "crossfadeProgress", 0f, 1f).apply {
            duration = 400
            interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    completeCrossfade()
                }
            })
            start()
        }
    }

    private fun completeCrossfade() {
        currentBitmap?.recycle()
        currentBitmap = nextBitmap
        nextBitmap = null
        crossfadeProgress = 0f
        loadImageAsync(false)
        morph = null
        progress = 0f
        currentShape = nextShape
        scheduleNextImageSwitch()
    }

    fun startAutoMorphing() {
        stopAutoMorphing()
        if (drawableResIds.isNotEmpty()) scheduleNextImageSwitch()
    }

    fun stopAutoMorphing() {
        handler.removeCallbacksAndMessages(null)
        crossfadeAnimator?.cancel()
        progressAnimator?.cancel()
        crossfadeAnimator = null
        progressAnimator = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()
        val size = min(width, height).toFloat()
        morph?.toPath(progress, path) ?: currentShape.toPath(path)

        path.computeBounds(boundsRect, true)
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

        canvas.withSave {
            clipPath(path)
            currentBitmap?.let { bmp ->
                // Calculate position to center the bitmap
                val left = (width - bmp.width) / 2f
                val top = (height - bmp.height) / 2f

                if (nextBitmap != null) {
                    paint.alpha = (255 * (1 - crossfadeProgress)).toInt()
                    canvas.drawBitmap(bmp, left, top, paint)

                    nextBitmap?.let { nextBmp ->
                        paint.alpha = (255 * crossfadeProgress).toInt()
                        canvas.drawBitmap(nextBmp, left, top, paint)
                    }
                    paint.alpha = 255
                } else {
                    canvas.drawBitmap(bmp, left, top, null)
                }
            } ?: run {
                drawPath(path, paint)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cleanup()
    }

    private fun cleanup() {
        stopAutoMorphing()
        scope.cancel()
        resetBitmaps()
    }

    private fun resetBitmaps() {
        currentBitmap?.recycle()
        nextBitmap?.recycle()
        currentBitmap = null
        nextBitmap = null
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