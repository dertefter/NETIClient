package com.dertefter.neticlient.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.news.NewsItem
import com.dertefter.neticlient.data.model.schedule.Day
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDayDashboardBinding
import com.dertefter.neticlient.databinding.FragmentNewsDashboardBinding
import com.dertefter.neticlient.databinding.ItemDashboardHeaderBinding
import com.dertefter.neticlient.ui.news.NewsAdapter
import com.dertefter.neticlient.ui.schedule.ScheduleUiState
import com.dertefter.neticlient.ui.schedule.week.day.TimesAdapter
import java.time.LocalDate
import java.time.LocalTime
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

    val adapter = TimesAdapter(fragment)

    var scheduleUiState: ScheduleUiState = ScheduleUiState(responseType = ResponseType.LOADING)

    var weekLabel: String = ""
    var currentDate: String = ""

    fun updateHeader(weekLabel: String, currentDate: String) {
        this.weekLabel = weekLabel
        this.currentDate = currentDate
        Log.e("updateHeader", "$weekLabel $currentDate")
        notifyItemChanged(0)
    }

    fun updateNews(list: List<NewsItem>) {
        val border = if (list.size <= 12) {list.size} else 12
        newsList = list
        val slicedList = newsList.subList(0, border - 1)
        newsAdapter.submitList(slicedList)
    }

    fun updateScheduleState(scheduleUiState: ScheduleUiState) {
        this.scheduleUiState = scheduleUiState
        notifyItemChanged(1)
    }

    fun updateTimeAndDate(date: LocalDate, time: LocalTime) {
        adapter.updateTimeAndDate(time, date)
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
                HeaderViewHolder(binding)
            }
            TYPE_SCHEDULE -> {
                val binding = FragmentDayDashboardBinding.inflate(layoutInflater, parent, false)
                ScheduleViewHolder(binding, fragment, adapter)
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
            is HeaderViewHolder -> holder.bind(weekLabel, currentDate)
            is ScheduleViewHolder -> holder.bind(state = scheduleUiState)
            is NewsListViewHolder -> holder.bind()
        }
    }

    class HeaderViewHolder(
        private val binding: ItemDashboardHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weekLabel: String, currentDate: String) {
            binding.weekLabel.text = weekLabel
            binding.currentDate.text = currentDate
        }
    }

    class ScheduleViewHolder(
        private val binding: FragmentDayDashboardBinding,
        val fragment: DashboardFragment,
        val adapter: TimesAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: ScheduleUiState) {
            binding.buttonScheduleAll.setOnClickListener {
                fragment.navigateTo(R.id.scheduleFragment)
            }

            binding.buttonSessiaSchedule.setOnClickListener {
                fragment.navigateTo(R.id.sessiaScheduleFragment)
            }

            binding.buttonPersonSearch.setOnClickListener {
                fragment.navigateTo(R.id.personSearchFragment)
            }

            binding.group.setOnClickListener {
                fragment.openGroupSearchDialog()
            }

            binding.searchGroupButton.setOnClickListener {
                fragment.openGroupSearchDialog()
            }

            when(state.responseType){
                ResponseType.SUCCESS -> onSuccess(state)
                ResponseType.LOADING -> onLoading(state)
                ResponseType.ERROR -> onError(state)
            }
        }

        fun onSuccess(state: ScheduleUiState) {
            binding.skeleton.visibility = View.GONE
            binding.schedule.visibility = View.VISIBLE
            binding.loadFail.root.visibility = View.GONE
            val schedule = state.schedule

            if (schedule == null){
                onError(state)
                return
            }

            val currentDate = CurrentTimeObject.currentDateLiveData.value
            val currentTime = CurrentTimeObject.currentTimeLiveData.value

            val nexDayWithLessons = schedule?.findNextDayWithLessonsAfter(currentDate, currentTime)

            val group = state.group
            if (group.isNullOrEmpty()){
                binding.noGroup.visibility = View.VISIBLE
            } else {
                binding.noGroup.visibility = View.GONE
                binding.group.text = group
                if (nexDayWithLessons != null){
                    adapter.setData(nexDayWithLessons)
                    binding.recyclerView.adapter = adapter
                    binding.recyclerView.layoutManager = LinearLayoutManager(itemView.context)
                    val todayDate = LocalDate.now()
                    val date = nexDayWithLessons.getDate()
                    when (date) {
                        todayDate ->  {
                            binding.dateTextView.text = fragment.getString(R.string.classses_for) + " " + fragment.getString(R.string.today) + " • "
                        }
                        todayDate.plusDays(1) -> {
                            binding.dateTextView.text = fragment.getString(R.string.classses_for) + " " + fragment.getString(R.string.tomorrow) + " • "
                        }
                        else -> {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM")
                            val dateString = date?.format(formatter)
                            binding.dateTextView.text = fragment.getString(R.string.classses_for) + " " + dateString + " • "
                        }
                    }

                }
            }
        }

        fun onLoading(state: ScheduleUiState) {
            val group = state.group
            adapter.clearData()
            binding.loadFail.root.visibility = View.GONE
            if (group.isNullOrEmpty()){
                binding.noGroup.visibility = View.VISIBLE
                binding.skeleton.visibility = View.GONE
            } else {
                binding.noGroup.visibility = View.GONE
                binding.skeleton.visibility = View.VISIBLE
                binding.group.text = group
            }

        }

        fun onError(state: ScheduleUiState) {
            adapter.clearData()
            binding.loadFail.root.visibility = View.VISIBLE
            binding.skeleton.visibility = View.GONE
            binding.schedule.visibility = View.GONE
            binding.loadFail.buttonRetry.setOnClickListener {
                fragment.scheduleViewModel.updateSchedule(group = state.group.orEmpty())
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
