package com.dertefter.neticlient.common

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.LoadingViewBinding
import com.dertefter.neticore.network.ResponseType
import kotlin.math.max

class CustomPullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private enum class GestureState {
        IDLE, PULLING, RETURNING
    }

    private var currentGestureState = GestureState.IDLE
    private var currentResponseType: ResponseType? = null

    val binding: LoadingViewBinding =
        LoadingViewBinding.inflate(LayoutInflater.from(context), this, false)

    private val statusContainer: View = binding.root
    private val statusLoadingView: View = binding.statusLoading
    private val statusSuccessView: View = binding.statusDone
    private val statusErrorView: View = binding.statusFail

    private var contentView: View? = null

    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var initialMotionY = 0f
    private var isBeingDragged = false
    private var refreshTriggerDistance = 0f
    private val DRAG_RESISTANCE = 0.5f

    private var onRefreshListener: (() -> Unit)? = null
    private val hideHandler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    init {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.TOP
        addView(statusContainer, lp)

        statusContainer.visibility = View.GONE
        statusContainer.post {
            refreshTriggerDistance = statusContainer.height.toFloat()
            statusContainer.translationY = -refreshTriggerDistance
        }

        statusErrorView.setOnClickListener {
            setStatus(ResponseType.LOADING)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 2) {
            throw IllegalStateException("CustomPullToRefreshLayout может содержать только один дочерний View для контента.")
        }
        contentView = getChildAt(0).takeIf { it != statusContainer } ?: getChildAt(1)
        if (contentView == null) {
            throw IllegalStateException("Добавьте контент (например, RecyclerView) внутрь CustomPullToRefreshLayout.")
        }
        statusContainer.bringToFront()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (contentView?.canScrollVertically(-1) == true) {
            return false
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialMotionY = ev.y
                isBeingDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = ev.y - initialMotionY
                if (dy > touchSlop && !isBeingDragged) {
                    isBeingDragged = true
                    currentGestureState = GestureState.PULLING
                    statusContainer.visibility = View.VISIBLE
                    showOnlyView(statusLoadingView)
                }
            }
        }
        return isBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                if (isBeingDragged) {
                    val pullDistance = (ev.y - initialMotionY) * DRAG_RESISTANCE
                    moveViews(max(0f, pullDistance))
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isBeingDragged) {
                    isBeingDragged = false
                    if ((contentView?.translationY ?: 0f) >= refreshTriggerDistance) {
                        setStatus(ResponseType.LOADING)
                    } else {
                        animateToInitialPosition()
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun moveViews(offset: Float) {
        contentView?.translationY = offset
        statusContainer.translationY = offset - refreshTriggerDistance
    }

    private fun animateToInitialPosition() {
        currentGestureState = GestureState.RETURNING
        val currentTranslation = contentView?.translationY ?: 0f
        ValueAnimator.ofFloat(currentTranslation, 0f).apply {
            duration = 300
            addUpdateListener {
                val value = it.animatedValue as Float
                moveViews(value)
            }
            doOnEnd {
                currentGestureState = GestureState.IDLE
                statusContainer.visibility = View.GONE
                currentResponseType = null
            }
            start()
        }
    }

    private fun animateToVisiblePosition() {
        currentGestureState = GestureState.RETURNING
        val currentTranslation = contentView?.translationY ?: 0f
        ValueAnimator.ofFloat(currentTranslation, refreshTriggerDistance).apply {
            duration = 300
            addUpdateListener {
                val value = it.animatedValue as Float
                moveViews(value)
            }
            doOnEnd {
                currentGestureState = GestureState.IDLE
            }
            start()
        }
    }

    private fun showOnlyView(viewToShow: View) {
        binding.statusLoading.visibility =
            if (viewToShow == binding.statusLoading) View.VISIBLE else View.GONE
        binding.statusDone.visibility =
            if (viewToShow == binding.statusDone) View.VISIBLE else View.GONE
        binding.statusFail.visibility =
            if (viewToShow == binding.statusFail) View.VISIBLE else View.GONE
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        this.onRefreshListener = listener
    }

    fun setStatus(type: ResponseType) {
        if (type == ResponseType.SUCCESS && currentResponseType == ResponseType.SUCCESS) {
            return
        }

        hideRunnable?.let { hideHandler.removeCallbacks(it) }

        currentResponseType = type
        statusContainer.visibility = View.VISIBLE

        when (type) {
            ResponseType.LOADING -> {
                animateToVisiblePosition()
                showOnlyView(binding.statusLoading)
                onRefreshListener?.invoke()
            }
            ResponseType.SUCCESS -> {
                showOnlyView(binding.statusDone)
                hideRunnable = Runnable { animateToInitialPosition() }
                hideHandler.postDelayed(hideRunnable!!, 1200)
            }
            ResponseType.ERROR -> {
                showOnlyView(binding.statusFail)
                hideRunnable = Runnable { animateToInitialPosition() }
                hideHandler.postDelayed(hideRunnable!!, 6000)
            }
        }
    }
}
