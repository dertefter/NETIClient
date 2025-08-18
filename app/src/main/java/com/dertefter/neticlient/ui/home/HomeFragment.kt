package com.dertefter.neticlient.ui.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.HorizontalSpaceItemDecoration
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentHomeBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.messages.MessagesViewModel
import com.dertefter.neticlient.ui.news.NewsAdapter
import com.dertefter.neticlient.ui.news.NewsViewModel
import com.dertefter.neticlient.ui.news.PromoAdapter
import com.dertefter.neticlient.ui.schedule.ScheduleUiState
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.schedule.week.day.TimesAdapter
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.HeroCarouselStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!

    val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val newsViewModel: NewsViewModel by activityViewModels()
    private val messagesViewModel: MessagesViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    private val homeViewModel: HomeViewModel by viewModels()

    var scheduleTimesAdapter: TimesAdapter? = null
    var newsAdapter: NewsAdapter? = null
    var promoAdapter: PromoAdapter? = null

    private var isInitialFilterSetup = true

    fun navigateTo(routeId: Int, args: Bundle? = null){
        findNavController().navigate(
            routeId,
            args,
            Utils.getNavOptions(),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (promoAdapter == null){
            promoAdapter = PromoAdapter(){
                    promoItem ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    promoItem.link.toUri())
                startActivity(intent)
            }
        }
        binding.promoRecyclerView.adapter = promoAdapter

        val promoLayoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

        binding.promoRecyclerView.setLayoutManager(promoLayoutManager)

        binding.promoRecyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(R.dimen.margin_min)
        )

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView( binding.promoRecyclerView)


        binding.searchBar.setOnClickListener {
            findNavController().navigate(
                R.id.searchFragment,
                null,
                Utils.getNavOptions(),
            )
        }

        newsViewModel.updatePromoList()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.promoListFlow.collect { promoList ->
                    if (promoList.isNullOrEmpty()){
                        binding.promoRecyclerView.isGone = true
                    }else{
                        promoAdapter?.submitList(promoList)
                        binding.promoRecyclerView.isGone = false
                    }

                }
            }
        }

        if (scheduleTimesAdapter == null) scheduleTimesAdapter = TimesAdapter(this)
        binding.scheduleRv.adapter = scheduleTimesAdapter
        binding.scheduleRv.layoutManager  = LinearLayoutManager(requireContext())

        if (newsAdapter == null){
            newsAdapter = NewsAdapter(
                { newsItem, color ->
                openNewsDetail(newsItem.id, newsItem.imageUrl, color)
            }
            )
        }
        binding.newsRv.adapter = newsAdapter
        binding.newsRv.layoutManager  = LinearLayoutManager(requireContext())

        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))

        binding.appbar.setExpanded(!homeViewModel.appVarIsLifted)

        binding.appbar.addOnOffsetChangedListener { appbar, p ->
            homeViewModel.appVarIsLifted = appbar.isLifted
        }

        CurrentTimeObject.currentTimeLiveData.observe(viewLifecycleOwner){ time ->
            val date = CurrentTimeObject.currentDateLiveData.value
            if (date != null) {
                scheduleTimesAdapter?.updateTimeAndDate(time, date)
            }
        }

        binding.scrollview.setOnScrollChangeListener { v: View, _, scrollY, _, _ ->
            val view = v as NestedScrollView
            if (view.getChildAt(0).bottom * 0.5 <= view.height + scrollY) {
                newsViewModel.fetchNews()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.scheduleState.collect { state ->
                    binding.buttonScheduleAll.setOnClickListener {
                        navigateTo(R.id.scheduleFragment)
                    }

                    binding.groupButton.setOnClickListener {
                        openGroupSearchDialog()
                    }

                    binding.searchGroupButton.setOnClickListener {
                        openGroupSearchDialog()
                    }

                    if (state.group.isNullOrEmpty()){
                        onSuccessSchedule(state)
                    }else{
                        when(state.responseType){
                            ResponseType.SUCCESS -> onSuccessSchedule(state)
                            ResponseType.LOADING -> onLoadingSchedule(state)
                            ResponseType.ERROR -> onErrorSchedule(state)
                        }
                    }

                }
            }
        }

        scheduleViewModel.updateWeekNumber()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.weekLabelFlow.collectLatest { weekLabel ->
                    val locale = Locale.getDefault()
                    val dateFormat = SimpleDateFormat("d MMMM", locale)
                    val currentDate = dateFormat.format(Date())
                    if (weekLabel != null){
                        updateHeader(weekLabel, currentDate)
                    } else {
                        updateHeader("", currentDate)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStateFlow.collect { authState ->
                    messagesViewModel.updateCount()
                }

            }
        }


        binding.newsRv.addItemDecoration(VerticalSpaceItemDecoration(R.dimen.margin_micro))

        newsViewModel.fetchNews(true)
        scheduleViewModel.updateWeekLabel()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.newsListFlow.collect { newsList ->
                    if (newsList != null){
                        newsAdapter?.updateItems(newsList)
                    }

                }
            }
        }

        binding.filterChips.setOnCheckedStateChangeListener { group, checkedIds ->
            if (isInitialFilterSetup) {
                isInitialFilterSetup = false
                return@setOnCheckedStateChangeListener
            }

            val checkedId = checkedIds.firstOrNull()
            if (checkedId != null) {
                when (checkedId) {
                    R.id.filter_all -> newsAdapter?.updateFilter(null)
                    R.id.filter_news -> newsAdapter?.updateFilter("новости")
                    R.id.filter_obj -> newsAdapter?.updateFilter("объявления")
                    R.id.filter_interview -> newsAdapter?.updateFilter("интервью")
                    R.id.filter_photo -> newsAdapter?.updateFilter("фото")
                    else -> newsAdapter?.updateFilter(null)
                }
                newsViewModel.fetchNews(true)
            }
        }

        binding.filterChips.check(R.id.filter_all)
        isInitialFilterSetup = false

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateToolbar(title: String, subtitle: String){
       // if (title == binding.toolbar.title && subtitle == binding.toolbar.subtitle) return
       // binding.toolbar.alpha = 0f
       // binding.toolbar.title = title
       // binding.toolbar.subtitle = subtitle
       // ObjectAnimator.ofFloat(binding.toolbar, "alpha", 0f, 1f).start()
    }

    fun openNewsDetail(id: String, imageUrl: String?, color: Int) {
        val bundle = Bundle()
        bundle.putString("newsId", id)
        bundle.putString("imageUrl", imageUrl)
        bundle.putInt("color", color)
        findNavController().navigate(
            R.id.newsDetailFragment,
            bundle,
            Utils.getNavOptions(),
        )
    }

    fun updateHeader(weekLabel: String, currentDate: String) {
        binding.weekLabel.text = weekLabel
    }

    fun onSuccessSchedule(state: ScheduleUiState) {

        binding.skeletonSchedule.visibility = View.GONE
        binding.schedule.visibility = View.VISIBLE
        binding.loadFailSchedule.root.visibility = View.GONE
        val schedule = state.schedule

        if (schedule == null && !state.group.isNullOrEmpty()){
            onErrorSchedule(state)
            return
        }

        val currentDate = CurrentTimeObject.currentDateLiveData.value
        val currentTime = CurrentTimeObject.currentTimeLiveData.value

        val nexDayWithLessons = schedule?.findNextDayWithLessonsAfter(currentDate, currentTime)
        val group = state.group
        if (group.isNullOrEmpty()){
            binding.noGroup.visibility = View.VISIBLE
            binding.schedule.visibility = View.GONE
        } else {
            binding.noGroup.visibility = View.GONE
            binding.schedule.visibility = View.VISIBLE
            binding.groupButton.text = group
            binding.groupButton.isGone = group.isEmpty()
            binding.noLessons.isGone = nexDayWithLessons != null

            if (nexDayWithLessons != null){
                scheduleTimesAdapter?.setData(nexDayWithLessons)
                val todayDate = LocalDate.now()
                val date = nexDayWithLessons.getDate()
                when (date) {
                    todayDate ->  {
                        binding.dateTextView.text = getString(R.string.classses_for) + " " + getString(R.string.today)
                    }
                    todayDate.plusDays(1) -> {
                        binding.dateTextView.text = getString(R.string.classses_for) + " " + getString(R.string.tomorrow)
                    }
                    else -> {
                        val formatter = DateTimeFormatter.ofPattern("d MMMM")
                        val dateString = date?.format(formatter)
                        binding.dateTextView.text = getString(R.string.classses_for) + " " + dateString
                    }
                }
            } else {
                binding.dateTextView.text = ""
            }
            binding.dateTextView.isGone = binding.dateTextView.text == ""
        }
    }

    fun onLoadingSchedule(state: ScheduleUiState) {
        val group = state.group
        scheduleTimesAdapter?.clearData()
        binding.loadFailSchedule.root.visibility = View.GONE
        if (group.isNullOrEmpty()){
            binding.noGroup.visibility = View.VISIBLE
            binding.skeletonSchedule.visibility = View.GONE
        } else {
            binding.noGroup.visibility = View.GONE
            binding.skeletonSchedule.visibility = View.VISIBLE
            binding.groupButton.text = group
        }
    }

    fun onErrorSchedule(state: ScheduleUiState) {
        scheduleTimesAdapter?.clearData()
        binding.loadFailSchedule.root.visibility = View.VISIBLE
        binding.skeletonSchedule.visibility = View.GONE
        binding.schedule.visibility = View.GONE
        binding.loadFailSchedule.buttonRetry.setOnClickListener {
            scheduleViewModel.updateSchedule(group = state.group.orEmpty())
        }
    }

    fun openGroupSearchDialog(){
        SearchGroupBottomSheet().show(
            requireActivity().supportFragmentManager,
            SearchGroupBottomSheet.TAG
        )
    }
}
