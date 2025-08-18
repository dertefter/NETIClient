package com.dertefter.neticlient.common


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.OverScroller
import androidx.core.content.res.ResourcesCompat
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.google.android.material.color.MaterialColors
import kotlin.math.max

class HorizontalScrollableBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle), GestureDetector.OnGestureListener {

    var onBarSelectedListener: ((item: SessiaResultSemestr, position: Int) -> Unit)? = null

    private val gestureDetector = GestureDetector(context, this)
    private val scroller = OverScroller(context)

    // Параметры для текста
    private var labelTypeface: Typeface = Typeface.DEFAULT
    private var labelTextSize = dp(14).toFloat()
    private var labelTextColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)

    // Параметры для столбцов
    private var barHeight = dp(48) // Было barWidth
    private var barSpacing = dp(4)
    private var barDrawable: Drawable? = null
    private var maxIconDrawable: Drawable? = null

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(this@HorizontalScrollableBarChartView, com.google.android.material.R.attr.colorSurface)
        style = Paint.Style.FILL
    }

    var data: List<SessiaResultSemestr> = emptyList()
        set(value) {
            val oldValues = animatedValues.toList()
            val newValues = value.map { it.srScoreSem ?: 0f }
            field = value

            maxValue = newValues.maxOrNull() ?: 0f

            if (value.isNotEmpty()) {
                selectedIndex = value.lastIndex
                post {
                    onBarSelectedListener?.invoke(value.last(), value.lastIndex)
                }
            } else {
                selectedIndex = null
            }

            animatedValues = MutableList(newValues.size) { oldValues.getOrNull(it) ?: 0f }

            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 500
                addUpdateListener { anim ->
                    val progress = anim.animatedValue as Float
                    animatedValues = animatedValues.mapIndexed { i, start ->
                        start + (newValues[i] - start) * progress
                    }.toMutableList()
                    invalidate()
                }
                start()
            }

            // Автоматическая прокрутка к последнему элементу по вертикали
            post {
                scrollYOffset = (data.size * (barHeight + barSpacing) - height + paddingTop + paddingBottom).toFloat()
                    .coerceAtLeast(0f)
            }
        }

    private var maxValue = 0f
    private var selectedIndex: Int? = null
    private var scrollYOffset = 0f // Было scrollXOffset

    private var animatedValues = mutableListOf<Float>()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ScrollableBarChartView, 0, 0).apply {
            try {
                labelTextSize = getDimension(R.styleable.ScrollableBarChartView_labelTextSize, labelTextSize)
                labelPaint.textSize = labelTextSize
                labelPaint.color = labelTextColor

                labelTypeface = loadTypeface(
                    getResourceId(R.styleable.ScrollableBarChartView_labelFontFamily, 0),
                    getString(R.styleable.ScrollableBarChartView_labelFontFamily), Typeface.BOLD
                )
                labelPaint.typeface = labelTypeface

                // Используем barHeight вместо barWidth
                barHeight = getDimensionPixelSize(R.styleable.ScrollableBarChartView_barWidth, barHeight)
                barSpacing = getDimensionPixelSize(R.styleable.ScrollableBarChartView_barSpacing, barSpacing)
                barDrawable = getDrawable(R.styleable.ScrollableBarChartView_barDrawable)
                maxIconDrawable = getDrawable(R.styleable.ScrollableBarChartView_maxIconDrawable)
            } finally {
                recycle()
            }
        }
        isClickable = true
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        // Ширина области для графика
        val chartWidth = width - paddingLeft - paddingRight - dp(16)
        val startY = -scrollYOffset.toInt()
        val maxIndex = data.indexOfFirst { it.srScoreSem == maxValue }

        val selectedColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary)
        val unselectedColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryContainer)

        data.forEachIndexed { index, item ->
            val value = animatedValues.getOrNull(index) ?: 0f
            val widthRatio = if (maxValue != 0f) value / maxValue else 0f
            // Минимальная ширина, чтобы текст поместился
            val minWidth = (labelTextSize * 2.5).toInt()
            val normalizedWidth = (chartWidth * widthRatio).toInt().coerceAtLeast(minWidth)

            // Расчет координат для горизонтального столбца
            val barTop = startY + paddingTop + index * (barHeight + barSpacing)
            val barLeft = paddingLeft
            val barRect = Rect(barLeft, barTop, barLeft + normalizedWidth, barTop + barHeight)

            val tint = if (selectedIndex == index) selectedColor else unselectedColor

            barDrawable?.mutate()?.apply {
                setTint(tint)
                setBounds(barRect)
                draw(canvas)
            } ?: Paint().apply {
                color = tint
                canvas.drawRect(barRect, this)
            }

            // Иконка для максимального значения
            if (index == maxIndex) {
                maxIconDrawable?.mutate()?.apply {
                    // Размещаем иконку в конце самого длинного столбца
                    setBounds(
                        barRect.right - barHeight, barRect.top,
                        barRect.right, barRect.bottom
                    )
                    draw(canvas)
                }
            }

            // Круг и текст теперь рисуются в начале столбца
            val circleRadius = barHeight/3f
            val circleCenterY = barRect.centerY().toFloat()
            val circleCenterX = (barRect.left + circleRadius + barHeight/6).toFloat()

            canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circlePaint)

            val textY = circleCenterY - (labelPaint.descent() + labelPaint.ascent()) / 2f
            canvas.drawText(item.title, circleCenterX, textY, labelPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            if (!result) {
                performClick()
            }
        }
        return true
    }

    // Обновляем выбор по координате Y
    private fun updateSelection(y: Float) {
        val touchedIndex = ((y + scrollYOffset - paddingTop) / (barHeight + barSpacing)).toInt()

        if (touchedIndex in data.indices) {
            if (selectedIndex != touchedIndex) {
                selectedIndex = touchedIndex
                onBarSelectedListener?.invoke(data[touchedIndex], touchedIndex)
                invalidate()
            }
        }
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollYOffset = scroller.currY.toFloat() // Используем currY
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDown(e: MotionEvent) = true

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        updateSelection(e.y) // Передаем Y координату
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        // Вычисляем максимальное смещение по Y
        val maxScrollY = max(0, data.size * (barHeight + barSpacing) - height + paddingTop + paddingBottom)
        // Прокручиваем по Y
        scrollYOffset = (scrollYOffset + distanceY).coerceIn(0f, maxScrollY.toFloat())
        invalidate()
        return true
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        // Вычисляем максимальное смещение по Y
        val maxScrollY = max(0, data.size * (barHeight + barSpacing) - height + paddingTop + paddingBottom)
        // Fling по Y
        scroller.fling(0, scrollYOffset.toInt(), 0, -velocityY.toInt(), 0, 0, 0, maxScrollY)
        invalidate()
        return true
    }


    fun setSelectedPosition(position: Int) {
        if (position in data.indices) {
            selectedIndex = position
            onBarSelectedListener?.invoke(data[position], position)
            invalidate()
        }
    }

    private fun dp(value: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    ).toInt()

    private fun loadTypeface(resId: Int, family: String?, style: Int) = when {
        resId != 0 -> ResourcesCompat.getFont(context, resId) ?: Typeface.DEFAULT
        !family.isNullOrBlank() -> Typeface.create(family, style)
        else -> Typeface.DEFAULT
    }
}