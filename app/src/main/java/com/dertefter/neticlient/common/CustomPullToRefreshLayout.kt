package com.dertefter.neticlient.common

import android.animation.ValueAnimator
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.PathInterpolator
import android.widget.LinearLayout
import com.dertefter.neticlient.R
import kotlin.math.abs

class CustomPullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private enum class State {
        IDLE,
        PULLING,
        REFRESHING,
        ERROR
    }

    private var currentState = State.IDLE

    private var headerView: View
    private var loadingContainer: View

    private var loadingIndicator: View
    private var preLoadingIndicator: View
    private var errorContainer: View
    private var contentView: View? = null

    private var headerHeight = 0
    private var initialY = 0f
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    private val alphaInterpolator = PathInterpolator(0.3f, 0f, 0.8f, 0.15f)
    private var onRefreshListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null

    private val vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private var isManualRefresh = false

    init {
        orientation = VERTICAL
        gravity = Gravity.TOP

        headerView = LayoutInflater.from(context)
            .inflate(R.layout.loading_view, this, false)
        headerView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0)
        addView(headerView, 0)

        loadingContainer = headerView.findViewById(R.id.status_loading)
        errorContainer = headerView.findViewById(R.id.status_error)
        loadingIndicator = headerView.findViewById(R.id.loading_indicator)
        preLoadingIndicator = headerView.findViewById(R.id.pre_loading_indicator)

        errorContainer.setOnClickListener {
            isManualRefresh = false
            startRefreshing()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            contentView = getChildAt(1)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (headerHeight == 0) {
            headerView.measure(
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            headerHeight = headerView.measuredHeight
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - initialY

                val isPullingDown = deltaY > touchSlop && !canChildScrollUp() && currentState == State.IDLE
                val isSwipingError = currentState == State.ERROR && abs(deltaY) > touchSlop
                val isSwipingToCancel = currentState == State.REFRESHING && abs(deltaY) > touchSlop

                if (isPullingDown) {
                    currentState = State.PULLING
                    return true
                }

                if (isSwipingError || isSwipingToCancel) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (currentState == State.PULLING) {
                    val deltaY = event.y - initialY
                    val pullDistance = deltaY / 2.0f
                    setHeaderHeight(pullDistance.toInt())

                    val rotation = (pullDistance / headerHeight) * 180f
                    preLoadingIndicator.rotation = rotation.coerceAtMost(360f)

                    preLoadingIndicator.visibility = View.VISIBLE
                    loadingIndicator.visibility = View.INVISIBLE

                    return true
                }
                if (currentState == State.ERROR || currentState == State.REFRESHING) {
                    val deltaY = event.y - initialY
                    val newHeight = headerHeight + deltaY
                    setHeaderHeight(newHeight.toInt())
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (currentState == State.PULLING) {
                    if (headerView.height >= headerHeight) {
                        smoothSetHeaderHeight(headerView.height, headerHeight)
                        isManualRefresh = true
                        startRefreshing()
                    } else {
                        smoothSetHeaderHeight(headerView.height, 0)
                        currentState = State.IDLE
                    }
                    return true
                }
                if (currentState == State.ERROR || currentState == State.REFRESHING) {
                    if (headerView.height < headerHeight / 2) {
                        if (currentState == State.REFRESHING) {
                            onCancelListener?.invoke()
                        }
                        stopRefreshing()
                    } else {
                        smoothSetHeaderHeight(headerView.height, headerHeight)
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        this.onRefreshListener = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        this.onCancelListener = listener
    }

    fun startRefreshing() {
        if (currentState != State.REFRESHING) {
            currentState = State.REFRESHING

            loadingContainer.visibility = View.VISIBLE
            errorContainer.visibility = View.GONE
            preLoadingIndicator.visibility = View.INVISIBLE
            loadingIndicator.visibility = View.VISIBLE

            if (headerHeight == 0) {
                headerView.measure(
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                headerHeight = headerView.measuredHeight
            }

            if (headerView.height < headerHeight) {
                smoothSetHeaderHeight(headerView.height, headerHeight)
            } else {
                setHeaderHeight(headerHeight)
            }

            if (isManualRefresh) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            onRefreshListener?.invoke()
        }
    }

    fun stopRefreshing() {
        currentState = State.IDLE
        smoothSetHeaderHeight(headerView.height, 0)
        preLoadingIndicator.rotation = 0f
    }

    fun showError() {
        currentState = State.ERROR
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE

        if (headerView.height < headerHeight) {
            smoothSetHeaderHeight(headerView.height, headerHeight)
        }
    }

    private fun setHeaderHeight(height: Int) {
        val targetHeight = height.coerceIn(0, (headerHeight * 1.5).toInt())
        headerView.layoutParams.height = targetHeight
        headerView.requestLayout()

        val progress = (targetHeight.toFloat() / headerHeight).coerceAtMost(1.5f)
        val scale = 0.8f + 0.2f * progress
        val alphaProgress = if (progress < 0.3) {
            0f
        } else {
            ((progress - 0.3) / (1f - 0.3f))
                .coerceIn(0.0, 1.0)
        }.toFloat()
        val alpha = alphaInterpolator.getInterpolation(alphaProgress)
        if (!scale.isNaN()){
            headerView.scaleX = scale
            headerView.scaleY = scale
        }
        headerView.alpha = alpha
    }

    private fun smoothSetHeaderHeight(from: Int, to: Int) {
        ValueAnimator.ofInt(from, to).apply {
            addUpdateListener {
                setHeaderHeight(it.animatedValue as Int)
            }
            duration = 200
            start()
        }
    }

    private fun canChildScrollUp(): Boolean {
        return contentView?.canScrollVertically(-1) ?: false
    }
}