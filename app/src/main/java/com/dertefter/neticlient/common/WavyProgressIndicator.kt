package com.dertefter.neticlient.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.google.android.material.progressindicator.LinearProgressIndicator

open class WavyProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearProgressIndicator(context, attrs, defStyleAttr) {

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C4AB6") // Цвет волны
        style = Paint.Style.STROKE
        strokeWidth = height.toFloat() // По высоте индикатора
        strokeCap = Paint.Cap.ROUND
    }

    private val wavePath = Path()
    private var wavePhase = 0f

    private val animator = ValueAnimator.ofFloat(0f, 2 * Math.PI.toFloat()).apply {
        duration = 2000L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            wavePhase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        // Запускаем анимацию, когда индикатор становится видимым
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        wavePath.reset()
        val amplitude = height / 3f
        val wavelength = width / 2f

        val centerY = height / 2f
        wavePath.moveTo(0f, centerY)

        var x = 0f
        while (x <= width) {
            val y = (amplitude * Math.sin(2 * Math.PI * (x / wavelength) + wavePhase)).toFloat()
            wavePath.lineTo(x, centerY + y)
            x += 5
        }

        canvas.drawPath(wavePath, wavePaint)
    }
}
