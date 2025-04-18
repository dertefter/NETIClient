package com.dertefter.neticlient.common

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AutoScrollHelper(
    private val recyclerView: RecyclerView,
    private val interval: Long = 8000L
) {
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val itemCount = recyclerView.adapter?.itemCount ?: return

            val nextItem = (layoutManager.findFirstVisibleItemPosition() + 1) % itemCount
            recyclerView.smoothScrollToPosition(nextItem)

            handler.postDelayed(this, interval)
        }
    }

    fun startAutoScroll() {
        handler.postDelayed(runnable, interval)
    }

    fun stopAutoScroll() {
        handler.removeCallbacks(runnable)
    }
}