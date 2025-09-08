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
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentHomeBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.messages.MessagesViewModel
import com.dertefter.neticlient.ui.news.NewsAdapter
import com.dertefter.neticlient.ui.news.NewsViewModel
import com.dertefter.neticlient.ui.news.PromoAdapter
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.schedule.week.day.TimesAdapter
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!

    val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val newsViewModel: NewsViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private val homeViewModel: HomeViewModel by viewModels()

    var scheduleTimesAdapter: TimesAdapter? = null
    var newsAdapter: NewsAdapter? = null
    var promoAdapter: PromoAdapter? = null

    private var isInitialFilterSetup = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun setupPromo(){
        val promoLayoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

        binding.promoRecyclerView.setLayoutManager(promoLayoutManager)
        if (promoAdapter == null){
            promoAdapter = PromoAdapter {
                    promoItem ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    promoItem.link.toUri())
                startActivity(intent)
            }
        }
        binding.promoRecyclerView.adapter = promoAdapter

        binding.promoRecyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(R.dimen.margin_min)
        )

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView( binding.promoRecyclerView)


    }

    fun setupButtons(){
        binding.tgButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://t.me/nstumobile_dev".toUri())
            requireContext().startActivity(intent)
            settingsViewModel.setTgShow(false)
        }

        binding.closeTg.setOnClickListener {
            settingsViewModel.setTgShow(false)
        }

        binding.groupButton.setOnClickListener {
            openGroupSearchDialog()
        }

        binding.searchGroupButton.setOnClickListener {
            openGroupSearchDialog()
        }

        binding.buttonScheduleAll.setOnClickListener {
            findNavController().navigate(
                R.id.scheduleFragment,
                null,
                Utils.getNavOptions(),
            )
        }

        binding.searchBar.setOnClickListener {
            findNavController().navigate(
                R.id.nav_graph_search,
                null,
                Utils.getNavOptions(),
            )
        }
    }

    fun setupSchedule(){
        scheduleTimesAdapter = TimesAdapter(
            emptyList(),
            viewLifecycleOwner,
            onLessonClick = { lesson ->
                val bottomSheet = LessonViewBottomSheetFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("lesson", lesson)
                    }
                }
                bottomSheet.show(parentFragmentManager, "LessonDetail")
            },
            onCurrentTimeSlotFound = { y ->
                homeViewModel.setNowY(y)
            },
            onLatestPastTimeSlotFound = { y ->
                homeViewModel.setPastY(y)
            },
            onFirstFutureTimeSlotFound = { y ->
                homeViewModel.setFutureY(y)
            }
        )
        binding.scheduleRv.adapter = scheduleTimesAdapter
        binding.scheduleRv.layoutManager  = LinearLayoutManager(requireContext())
        binding.scheduleRv.addItemDecoration(
            VerticalSpaceItemDecoration(
                R.dimen.radius_max,
                R.dimen.radius_micro
            )
        )
    }

    fun setupNews(){
        if (newsAdapter == null){
            newsAdapter = NewsAdapter(
                { newsItem, color ->
                    openNewsDetail(newsItem.id, newsItem.imageUrl, color)
                }
            )
        }
        binding.newsRv.adapter = newsAdapter
        binding.newsRv.layoutManager  = LinearLayoutManager(requireContext())
        binding.newsRv.addItemDecoration(
            VerticalSpaceItemDecoration(
                R.dimen.radius_max,
                R.dimen.radius_micro
            )
        )

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


    fun collectingNews(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.newsListFlow.collect { newsList ->
                    if (newsList != null){
                        newsAdapter?.updateItems(newsList)
                    }

                }
            }
        }
    }

    fun collectingGroup(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.currentGroup.collect { currentGroup ->
                    binding.noGroup.isGone = !currentGroup.isNullOrEmpty()
                    binding.groupButton.isGone = currentGroup.isNullOrEmpty()
                    if (currentGroup != null){
                        binding.groupButton.text = currentGroup
                        scheduleViewModel.updateScheduleForGroup(currentGroup)
                    }
                }
            }
        }
    }

    fun collectingNextDayWithLessons(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.nextDayWithLessons.collect { nextDayWithLessons ->
                    if (nextDayWithLessons != null) {
                        scheduleTimesAdapter?.updateData(nextDayWithLessons.times)
                        binding.scheduleRv.isGone = false
                        val date = nextDayWithLessons.getDate()
                        when (date) {
                            LocalDate.now() -> {
                                binding.dateTv1?.text = getString(R.string.classses_for)
                                binding.dateTv2?.text = getString(R.string.today)
                            }

                            LocalDate.now().plusDays(1) -> {
                                binding.dateTv1?.text = getString(R.string.classses_for)
                                binding.dateTv2?.text = getString(R.string.tomorrow)
                            }

                            else -> {
                                binding.dateTv1?.text = getString(R.string.classses_for)
                                val formatter = DateTimeFormatter.ofPattern("d MMMM")
                                val dateString = date?.format(formatter)
                                binding.dateTv2?.text = dateString
                            }
                        }
                    }
                    else {
                        scheduleTimesAdapter?.updateData(emptyList())
                        binding.dateTv1?.text = getString(R.string.near_lessons)
                        binding.dateTv2?.text = ""
                    }
                    binding.dateTv1?.isGone = binding.dateTv1?.text.isNullOrEmpty()
                    binding.dateTv2?.isGone = binding.dateTv2?.text.isNullOrEmpty()

                }
            }
        }
    }

    fun collectingStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.status.collect { status ->
                    Log.e("scheduleViewModel.status", status.toString())
                    when (status) {

                        com.dertefter.neticore.network.ResponseType.LOADING -> {
                            binding.loadFailSchedule!!.root.isGone = true
                            binding.skeletonSchedule.isGone = false
                            binding.scheduleRv.isGone = true
                        }
                        com.dertefter.neticore.network.ResponseType.SUCCESS -> {
                            binding.loadFailSchedule!!.root.isGone = true
                            binding.skeletonSchedule.isGone = true
                            binding.scheduleRv.isGone = false
                        }
                        com.dertefter.neticore.network.ResponseType.ERROR -> {
                            binding.loadFailSchedule!!.root.isGone = false
                            binding.skeletonSchedule.isGone = true
                            binding.scheduleRv.isGone = false
                        }
                    }

                }
            }
        }
    }

    fun collectingWeekLabel(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.weekLabel.collect { weekLabel ->
                    binding.weekLabel.text = weekLabel
                }
            }
        }
    }


    fun collectingAppbarAndOther() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    homeViewModel.appVarIsLifted.collect { isLifted ->
                        if (isLifted){
                            binding.appbar.isLifted = true
                            binding.appbar.setExpanded(false)
                        }

                    }
                }

                launch {
                    combine(
                        homeViewModel.nowY,
                        homeViewModel.pastY,
                        homeViewModel.futureY
                    ) { now, past, future ->
                        Triple(now, past, future)
                    }.collect { (now, past, future) ->
                        Log.e("zzz combine", "now: $now, past: $past, future: $future")

                        if (now != -1){
                            binding.scrollview.smoothScrollTo(0, now)
                        } else if (future != -1){
                            binding.scrollview.smoothScrollTo(0, future)
                        } else {
                            binding.scrollview.smoothScrollTo(0, past)
                        }


                    }
                }

            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))

        if (binding.xxxContainer != null){
            ViewCompat.setOnApplyWindowInsetsListener(binding.xxxContainer!!) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    right = bars.right,
                )
                WindowInsetsCompat.CONSUMED
            }
        }

        collectingAppbarAndOther()
        setupPromo()

        setupButtons()

        setupSchedule()

        setupNews()

        collectingNews()

        collectingGroup()

        collectingNextDayWithLessons()

        collectingWeekLabel()

        collectingStatus()

        newsViewModel.updatePromoList()
        scheduleViewModel.updateWeekLabel()
        scheduleViewModel.updateWeekNumber()


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


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.isTgShow.collect { isShow ->
                    binding.tgButton.isGone = !isShow
                }
            }
        }



        binding.scrollview.setOnScrollChangeListener { v: View, _, scrollY, _, _ ->
            val view = v as NestedScrollView

            if (scrollY > resources.displayMetrics.heightPixels * 2.5){
                binding.upFab?.isInvisible = false
                binding.upFab?.setOnClickListener {
                    binding.scrollview.smoothScrollTo(0, 0)
                    homeViewModel.setAppVarIsLifted(false)

                }
            }else{
                binding.upFab?.isInvisible = true
                binding.upFab?.setOnClickListener {

                }
            }

            homeViewModel.setScrollY(scrollY)

            if (view.getChildAt(0).bottom * 0.5 <= view.height + scrollY) {
                newsViewModel.fetchNews()
            }

        }



        scheduleViewModel.updateWeekNumber()




        newsViewModel.fetchNews(true)
        scheduleViewModel.updateWeekLabel()






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
        val action = HomeFragmentDirections
            .actionHomeFragmentToNewsDetailFragment(id, imageUrl, color)

        findNavController().navigate(action, Utils.getNavOptions())
    }


    fun openGroupSearchDialog(){
        SearchGroupBottomSheet().show(
            requireActivity().supportFragmentManager,
            SearchGroupBottomSheet.TAG
        )
    }
}
