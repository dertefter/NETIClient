package com.dertefter.neticlient.common.calendar_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import java.util.Calendar

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var rvMonths: RecyclerView
    private val events = mutableListOf<CalendarEvent>()
    private val baseCalendar = Calendar.getInstance()
    private lateinit var monthAdapter: MonthAdapter
    private var currentCenterPosition = Int.MAX_VALUE / 2
    private lateinit var snapHelper: PagerSnapHelper
    private val scrollListener = ScrollListener()

    // Коллбеки
    var onMonthChanged: ((firstDayOfMonth: Calendar) -> Unit)? = null
    var onDateSelected: ((selectedDate: Calendar) -> Unit)? = null

    // Текущая выбранная дата
    private var selectedDate: Calendar? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_calendar, this, true)

        rvMonths = view.findViewById(R.id.rvMonths)

        setupCalendar()
    }

    private fun setupCalendar() {
        monthAdapter = MonthAdapter(
            events = events,
            baseCalendar = baseCalendar,
            today = Calendar.getInstance(),
            selectedDate = selectedDate,
            onDateClickListener = { date ->
                selectedDate = date
                onDateSelected?.invoke(date)
                // Обновляем отображение
                monthAdapter.selectedDate = date
                monthAdapter.notifyDataSetChanged()
            }
        )

        snapHelper = PagerSnapHelper().apply {
            attachToRecyclerView(rvMonths)
        }

        rvMonths.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = monthAdapter
            scrollToPosition(currentCenterPosition)
            addOnScrollListener(scrollListener)
        }

        updateMonthTitle(currentCenterPosition)
    }

    private fun updateMonthTitle(position: Int) {
        val calendar = Calendar.getInstance().apply {
            time = baseCalendar.time
            add(Calendar.MONTH, position - (Int.MAX_VALUE / 2))
        }
    }

    fun addEvent(event: CalendarEvent) {
        events.add(event)
        monthAdapter.notifyDataSetChanged()
    }

    fun setEvents(newEvents: List<CalendarEvent>) {
        events.clear()
        events.addAll(newEvents)
        monthAdapter.notifyDataSetChanged()
    }

    fun setSelectedDate(date: Calendar) {
        selectedDate = date
        monthAdapter.selectedDate = date
        monthAdapter.notifyDataSetChanged()
        onMonthChanged?.invoke(date)
    }

    inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val snapView = snapHelper.findSnapView(recyclerView.layoutManager)
            snapView?.let {
                val position = recyclerView.layoutManager?.getPosition(it) ?: return
                if (position != currentCenterPosition) {
                    currentCenterPosition = position
                    updateMonthTitle(position)

                    // Вычисляем первый день месяца для коллбека
                    val firstDayOfMonth = Calendar.getInstance().apply {
                        time = baseCalendar.time
                        add(Calendar.MONTH, position - (Int.MAX_VALUE / 2))
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    onMonthChanged?.invoke(firstDayOfMonth)
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val snapView = snapHelper.findSnapView(recyclerView.layoutManager)
                snapView?.let {
                    val position = recyclerView.layoutManager?.getPosition(it) ?: return
                    currentCenterPosition = position
                    updateMonthTitle(position)

                    // Вычисляем первый день месяца для коллбека
                    val firstDayOfMonth = Calendar.getInstance().apply {
                        time = baseCalendar.time
                        add(Calendar.MONTH, position - (Int.MAX_VALUE / 2))
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    onMonthChanged?.invoke(firstDayOfMonth)
                }
            }
        }
    }
}