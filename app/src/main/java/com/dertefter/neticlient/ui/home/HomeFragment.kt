package com.dertefter.neticlient.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.updatePadding
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
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.HorizontalSpaceItemDecoration
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils.goingTo
import com.dertefter.neticlient.databinding.FragmentHomeBinding
import com.dertefter.neticlient.ui.news.NewsAdapter
import com.dertefter.neticlient.ui.news.NewsViewModel
import com.dertefter.neticlient.ui.news.PromoAdapter
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.schedule.TimesAdapter
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticore.NETICore
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var netiCore: NETICore
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val newsViewModel: NewsViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var scheduleTimesAdapter: TimesAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var promoAdapter: PromoAdapter

    private var isInitialFilterSetup = true
    private var hasScrolledToInitialLesson = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModels()
        requestInitialData()
    }

    private fun setupUI() {
        setupEdgeToEdge()
        setupPromo()
        setupButtons()
        setupSchedule()
        setupNews()
        setupScrollListener()
    }

    private fun setupEdgeToEdge() {
        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge(binding.appbar))
        if (binding.xxxContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.xxxContainer!!) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(right = bars.right)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun setupPromo() {
        promoAdapter = PromoAdapter { promoItem ->
            val intent = Intent(Intent.ACTION_VIEW, promoItem.link.toUri())
            startActivity(intent)
        }
        binding.promoRecyclerView.apply {
            layoutManager = CarouselLayoutManager(HeroCarouselStrategy()).apply {
                carouselAlignment = CarouselLayoutManager.ALIGNMENT_CENTER
            }
            adapter = promoAdapter
            //addItemDecoration(HorizontalSpaceItemDecoration(R.dimen.d2))
            val snapHelper = CarouselSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }


    }

    private fun setupButtons() {
        binding.tgButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://t.me/nstumobile_dev".toUri())
            requireContext().startActivity(intent)
            settingsViewModel.setTgShow(false)
        }
        binding.closeTg.setOnClickListener { settingsViewModel.setTgShow(false) }
        binding.groupButton.setOnClickListener { openGroupSearchDialog() }
        binding.searchGroupButton.setOnClickListener { openGroupSearchDialog() }
        binding.buttonScheduleAll.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToScheduleFragment()
            findNavController().goingTo(action)
        }
        binding.searchBar.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToNavGraphSearch()
            findNavController().goingTo(action)
        }
    }

    private fun setupSchedule() {
        scheduleTimesAdapter = TimesAdapter(
            emptyList(),
            viewLifecycleOwner,
            onLessonClick = { lesson ->
                LessonViewBottomSheetFragment().apply {
                    arguments = Bundle().apply { putParcelable("lesson", lesson) }
                }.show(parentFragmentManager, "LessonDetail")
            },
            onCurrentTimeSlotFound = { y -> homeViewModel.setNowY(y) },
            onLatestPastTimeSlotFound = { y -> homeViewModel.setPastY(y) },
            onFirstFutureTimeSlotFound = { y -> homeViewModel.setFutureY(y)},
            netiCore.personDetailFeature
        )
        binding.scheduleRv.apply {
            adapter = scheduleTimesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                VerticalSpaceItemDecoration()
            )
        }
    }

    private fun setupNews() {
        newsAdapter = NewsAdapter { newsItem, color ->
            openNewsDetail(newsItem.id, newsItem.imageUrl, color)
        }
        binding.newsRv.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                VerticalSpaceItemDecoration()
            )
            isNestedScrollingEnabled = false
        }

        binding.filterChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isInitialFilterSetup) return@setOnCheckedStateChangeListener

            val category = when (checkedIds.firstOrNull()) {
                R.id.filter_news -> "новости"
                R.id.filter_obj -> "объявления"
                R.id.filter_interview -> "интервью"
                R.id.filter_photo -> "фото"
                else -> null
            }
            newsAdapter.updateFilter(category)
            newsViewModel.fetchNews(true)
        }
    }

    private fun setupScrollListener() {
        binding.scrollview.setOnScrollChangeListener { v: View, _, scrollY, _, _ ->
            val view = v as NestedScrollView

            val isFabVisible = scrollY > resources.displayMetrics.heightPixels * 2.5
            binding.upFab?.isInvisible = !isFabVisible
            if (isFabVisible) {
                binding.upFab?.setOnClickListener {
                    binding.scrollview.smoothScrollTo(0, 0)
                    homeViewModel.setAppVarIsLifted(false)
                }
            } else {
                binding.upFab?.setOnClickListener(null)
            }

            homeViewModel.setScrollY(scrollY)

            if (view.getChildAt(0).bottom <= view.height + scrollY) {
                newsViewModel.fetchNews()
            }
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeSchedule() }
                launch { observeWeekLabel() }
                launch { observeInitialScrollPosition() }
                launch { observeNews() }
                launch { observePromo() }
                launch { observeTgButtonVisibility() }
                launch { observeAppBarState() }
            }
        }
    }
    private fun observeSchedule() {
        lifecycleScope.launch {
            combine(
                scheduleViewModel.currentGroup,
                scheduleViewModel.nextDayWithLessons,
                scheduleViewModel.status
            ) { currentGroup, nextDayWithLessons, status ->
                Triple(currentGroup, nextDayWithLessons, status)
            }.collect { (currentGroup, nextDayWithLessons, status) ->

                Log.e("observeSchedule", "($currentGroup, $nextDayWithLessons, $status)")


                binding.loadFailSchedule.root.isGone = status != com.dertefter.neticore.network.ResponseType.ERROR || currentGroup.isNullOrEmpty()
                binding.skeletonSchedule.isGone = status != com.dertefter.neticore.network.ResponseType.LOADING || currentGroup.isNullOrEmpty()
                binding.scheduleRv.isGone = status == com.dertefter.neticore.network.ResponseType.LOADING || currentGroup.isNullOrEmpty()
                binding.noGroup.isGone = !currentGroup.isNullOrEmpty()
                binding.groupButton.isGone = currentGroup.isNullOrEmpty()
                binding.buttonScheduleAll.isGone = currentGroup.isNullOrEmpty()

                if (currentGroup != null && status != com.dertefter.neticore.network.ResponseType.ERROR ) {
                    binding.groupButton.text = currentGroup
                    scheduleViewModel.updateScheduleForGroup(currentGroup)
                }

                if (nextDayWithLessons != null) {
                    scheduleTimesAdapter.updateData(nextDayWithLessons.times)
                    binding.scheduleRv.isGone = false

                    val date = nextDayWithLessons.getDate()
                    val (text1, text2) = when (date) {
                        LocalDate.now() -> getString(R.string.classses_for) to getString(R.string.today)
                        LocalDate.now().plusDays(1) -> getString(R.string.classses_for) to getString(R.string.tomorrow)
                        else -> {
                            val formatter = DateTimeFormatter.ofPattern("d MMMM")
                            getString(R.string.classses_for) to date?.format(formatter)
                        }
                    }
                    binding.dateTv1?.text = text1
                    binding.dateTv2?.text = text2
                } else {
                    scheduleTimesAdapter.updateData(emptyList())
                    binding.dateTv1?.text = getString(R.string.near_lessons)
                    binding.dateTv2?.text = ""
                }
                binding.dateTv1?.isGone = binding.dateTv1?.text.isNullOrEmpty()
                binding.dateTv2?.isGone = binding.dateTv2?.text.isNullOrEmpty()
            }
        }
    }


    private suspend fun observeWeekLabel() {
        scheduleViewModel.weekLabel.collect { weekLabel ->
            binding.weekLabel.text = weekLabel
        }
    }

    private suspend fun observeNews() {
        newsViewModel.newsListFlow.collect { newsList ->
            if (newsList != null) {
                newsAdapter.updateItems(newsList)
            }
        }
    }

    private suspend fun observePromo() {
        newsViewModel.promoListFlow.collect { promoList ->
            binding.promoRecyclerView.isGone = promoList.isNullOrEmpty()
            if (!promoList.isNullOrEmpty()) {
                promoAdapter.submitList(promoList)
            }
        }
    }

    private suspend fun observeTgButtonVisibility() {
        settingsViewModel.isTgShow.collect { isShow ->
            binding.tgButton.isGone = !isShow
        }
    }

    private suspend fun observeAppBarState() {
        homeViewModel.appVarIsLifted.collect { isLifted ->
            if (isLifted) {
                binding.appbar.isLifted = true
                binding.appbar.setExpanded(false)
            }
        }
    }

    private suspend fun observeInitialScrollPosition() {
        combine(
            homeViewModel.nowY,
            homeViewModel.pastY,
            homeViewModel.futureY
        ) { now, past, future ->
            Triple(now, past, future)
        }.collect { (now, past, future) ->
            if (hasScrolledToInitialLesson) return@collect

            val y = when {
                now != -1 -> now
                future != -1 -> future
                else -> past
            }

            if (y != -1) {
                binding.scrollview.post {
                    binding.scrollview.smoothScrollTo(0, y)
                    hasScrolledToInitialLesson = true
                }
            }
        }
    }

    private fun requestInitialData() {
        scheduleViewModel.updateWeekLabel()
        scheduleViewModel.updateWeekNumber()
        newsViewModel.updatePromoList()


        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            binding.filterChips.check(R.id.filter_all)
            isInitialFilterSetup = false
            newsViewModel.fetchNews(true)
        }
    }



    private fun openNewsDetail(id: String, imageUrl: String?, color: Int) {
        val action = HomeFragmentDirections.actionHomeFragmentToNewsDetailFragment(id, imageUrl, color)
        findNavController().goingTo(action)
    }

    private fun openGroupSearchDialog() {
        SearchGroupBottomSheet().show(requireActivity().supportFragmentManager, SearchGroupBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.scheduleRv.adapter = null
        binding.newsRv.adapter = null
        binding.promoRecyclerView.adapter = null
    }
}
