package com.dertefter.neticlient.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentCalendarBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    val binding get() = _binding!!

    val calendarViewModel: CalendarViewModel by activityViewModels()

    lateinit var adapter: CalendarDayListAdapter

    val scheduleViewModel: ScheduleViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun openNewsDetail(id: String) {
        val bundle = Bundle()
        bundle.putString("newsId", id)
        findNavController().navigate(
            R.id.newsDetailFragment,
            bundle,
            Utils.getNavOptions(),
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CalendarDayListAdapter(
            onItemClick = {},
            fragment = this
        )
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge(binding.appbar))



        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        val decoration = VerticalSpaceItemDecoration(R.dimen.margin_max, R.dimen.margin_micro)
        binding.recyclerview.addItemDecoration(decoration)

        binding.calendarView.onDateSelected = { date ->
            val position = adapter.findPositionByDate(date)
          binding.recyclerview.scrollToPosition(position)
            binding.appbar.setExpanded(false, true)
        }

        binding.filterSchedule.setOnLongClickListener {
            SearchGroupBottomSheet().show(
                requireActivity().supportFragmentManager,
                SearchGroupBottomSheet.TAG
            )
            true
        }

        binding.calendarView.onMonthChanged = { firstDate ->
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val selectedYear = firstDate.get(Calendar.YEAR)

            val monthFormatter = SimpleDateFormat("LLLL", Locale.getDefault())
            val month = monthFormatter.format(firstDate.time).replaceFirstChar { it.uppercaseChar() }

            val monthYear = if (selectedYear == currentYear) {
                month
            } else {
                "$month, $selectedYear"
            }
            binding.toolbar.title = monthYear

            val days = getDaysInMonth(firstDate)
            adapter.updateDates(days)
            val yearString = firstDate.get(Calendar.YEAR).toString()
            val monthString = (firstDate.get(Calendar.MONTH) + 1).toString()

            calendarViewModel.updateEventsForMonth(yearString, monthString)
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    calendarViewModel.getEventsFlowForMonth(yearString, monthString).collect { events ->
                        if (events != null){
                            binding.calendarView.setEvents(events)
                            adapter.updateEvents(events)
                        }
                    }
                }
            }


        }


        binding.calendarView.setSelectedDate(Calendar.getInstance())

        binding.filterEvents.setOnCheckedChangeListener { v, b ->
            adapter.updateFilterEvents(b)
        }

        binding.filterSchedule.setOnCheckedChangeListener { v, b ->
            adapter.updateFilterSchedule(b)
        }

        binding.filterChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val isFilterSchedule = checkedIds.contains(binding.filterSchedule.id)
            val isFilterEvents = checkedIds.contains(binding.filterEvents.id)
            adapter.updateFilterEvents(isFilterEvents)
            adapter.updateFilterSchedule(isFilterSchedule)
        }




    }

    fun getDaysInMonth(firstDate: Calendar): List<LocalDate> {
        val year = firstDate.get(Calendar.YEAR)
        val month = firstDate.get(Calendar.MONTH) // 0-based (январь = 0)

        // Создаём Calendar, установленный на первый день месяца
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return (1..daysInMonth).map { day ->
            LocalDate.of(year, month + 1, day) // LocalDate использует 1-based месяц
        }
    }



}
