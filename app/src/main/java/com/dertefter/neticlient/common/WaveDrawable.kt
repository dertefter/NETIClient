package com.dertefter.neticlient.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.sin

class WaveDrawable : Drawable() {

    private val paint = Paint().apply {
        color = Color.parseColor("#6C4EA0") // Цвет волны
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val path = Path()

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        path.reset()

        // Волнистая линия: синусоида
        val amplitude = height / 2
        val frequency = 2 * Math.PI / width
        val step = 5

        path.moveTo(0f, height / 2)

        for (x in 0..width.toInt() step step) {
            val y = (amplitude * sin(frequency * x)).toFloat() + amplitude
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
