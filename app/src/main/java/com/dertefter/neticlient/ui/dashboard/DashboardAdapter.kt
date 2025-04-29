package com.dertefter.neticlient.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDayDashboardBinding
import com.dertefter.neticlient.databinding.FragmentNewsDashboardBinding
import com.dertefter.neticlient.databinding.ItemDashboardHeaderBinding
import com.dertefter.neticlient.ui.news.NewsAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardAdapter(val fragment: DashboardFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var newsList = listOf<NewsItem>()
    private val newsAdapter = NewsAdapter(
        loadingViewsCount = 0,
        onItemClick = { newsItem, color ->
            val bundle = Bundle()
            bundle.apply {
                putString("newsId", newsItem.id)
                putString("imageUrl", newsItem.imageUrl)
                putInt("color", color)
            }
            fragment.navigateTo(R.id.newsDetailFragment, bundle)
        }
    )


    var group: String? = null
    var dayNumber: Int? = null
    var weekNumber: Int? = null

    var weekLabel: String = ""
    var currentDate: String = ""

    fun updateHeader(weekLabel: String, currentDate: String) {
        this.weekLabel = weekLabel
        this.currentDate = currentDate
        notifyItemChanged(0)
    }

    fun updateNews(list: List<NewsItem>) {
        val border = if (list.size <= 12) {list.size} else 12
        newsList = list
        val slicedList = newsList.subList(0, border - 1)
        newsAdapter.submitList(slicedList)
    }

    fun updateScheduleData(group: String, weekNumber: Int, dayNumber: Int) {
        Log.e("updateScheduleData", "$group, $weekNumber, $dayNumber")
        this.group = group
        this.dayNumber = dayNumber
        this.weekNumber = weekNumber
        notifyItemChanged(1)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SCHEDULE = 1
        private const val TYPE_NEWS_LIST = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            1 -> TYPE_SCHEDULE
            2 -> TYPE_NEWS_LIST
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemDashboardHeaderBinding.inflate(layoutInflater, parent, false)
                HeaderViewHolder(binding, weekLabel, currentDate)
            }
            TYPE_SCHEDULE -> {
                val binding = FragmentDayDashboardBinding.inflate(layoutInflater, parent, false)
                ScheduleViewHolder(binding, fragment, group, weekNumber, dayNumber)
            }
            TYPE_NEWS_LIST -> {
                val binding = FragmentNewsDashboardBinding.inflate(layoutInflater, parent, false)
                NewsListViewHolder(binding, newsAdapter, fragment)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind()
            is ScheduleViewHolder -> holder.bind()
            is NewsListViewHolder -> holder.bind()
        }
    }

    class HeaderViewHolder(
        private val binding: ItemDashboardHeaderBinding,
        val weekLabel: String,
        val currentDate: String
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.weekLabel.text = weekLabel
            binding.currentDate.text = currentDate
        }
    }

    class ScheduleViewHolder(
        private val binding: FragmentDayDashboardBinding,
        val fragment: DashboardFragment,
        val group: String?,
        val weekNumber: Int?,
        var dayNumber: Int?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {

            binding.buttonScheduleAll.setOnClickListener {
                fragment.navigateTo(R.id.scheduleFragment)
            }

            binding.buttonSessiaSchedule.setOnClickListener {
                fragment.navigateTo(R.id.sessiaScheduleFragment)
            }

            binding.buttonPersonSearch.setOnClickListener {
                fragment.navigateTo(R.id.personSearchFragment)
            }

            if (!group.isNullOrEmpty()){
                (binding.group.parent as View).visibility = View.VISIBLE
            } else {
                (binding.group.parent as View).visibility = View.INVISIBLE
            }
            binding.group.setOnClickListener {
                fragment.openGroupSearchDialog()
            }
            binding.group.text = group

            if (group.isNullOrEmpty()){
                binding.noGroup.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.dateGroupContainer.visibility = View.GONE
                binding.buttonsContainer.visibility = View.GONE
                binding.searchGroupButton.setOnClickListener {
                    fragment.openGroupSearchDialog()
                }
            } else {
                binding.dateGroupContainer.visibility = View.VISIBLE
                binding.noGroup.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.buttonsContainer.visibility = View.VISIBLE
            }


            binding.recyclerView.isNestedScrollingEnabled = false
            if (!group.isNullOrEmpty() && weekNumber != null && dayNumber != null) {
                fragment.scheduleViewModel.getScheduleLiveData(group).observe(fragment.viewLifecycleOwner) { response ->
                    if (response.responseType == ResponseType.SUCCESS && response.data != null) {
                        val schedule = response.data as Schedule
                        var week = schedule.weeks.find { it.weekNumber == weekNumber }!!
                        if (dayNumber == 7){
                            if (schedule.weeks.find { it.weekNumber == weekNumber + 1 } != null){
                                week = schedule.weeks.find { it.weekNumber == weekNumber + 1 }!!
                                dayNumber = 1
                            }else {
                                dayNumber = 6
                            }
                        }
                        val day =  week.days.find { it.dayNumber == dayNumber }
                        val times = day!!.times
                        val adapter = DashboardTimesAdapter(times, weekNumber, dayNumber!!, fragment)

                        val todayDate = LocalDate.now()
                        val date = day.getDate()

                        when (date) {
                            todayDate ->  {
                                binding.dateTextView.text = fragment.getString(R.string.classses_for) + " " + fragment.getString(R.string.today) + " • "
                            }
                            todayDate.plusDays(1) -> {
                                binding.dateTextView.text = fragment.getString(R.string.classses_for) + " " + fragment.getString(R.string.tomorrow) + " • "
                            }
                            else -> {
                                val formatter = DateTimeFormatter.ofPattern("dd.MM")
                                val dateString = date.format(formatter)
                                binding.dateTextView.text = fragment.getString(R.string.classses_for) + " " + dateString + " • "
                            }
                        }

                        adapter.setData(times, week.weekNumber, day.dayNumber)
                        binding.recyclerView.adapter = adapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

                    }
                }
            }
        }
    }

    class NewsListViewHolder(
        private val binding: FragmentNewsDashboardBinding, val adapter: NewsAdapter, val fragment: DashboardFragment
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.moreNews.setOnClickListener {
                fragment.navigateTo(R.id.newsFragment)
            }
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
            binding.recyclerView.isNestedScrollingEnabled = false
            val spacingInPixels = fragment.resources.getDimensionPixelSize(R.dimen.margin_min)
            binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spacingInPixels))
        }
    }
}
