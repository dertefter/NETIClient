package com.dertefter.neticlient.ui.dashboard

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDashboardBinding
import com.dertefter.neticlient.databinding.FragmentScheduleBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.messages.MessagesViewModel
import com.dertefter.neticlient.ui.news.NewsViewModel
import com.dertefter.neticlient.ui.profile.profile_dialog.ProfileDialogFragment
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonDetailViewModel
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.schedule.week.WeekFragment
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.widgets.schedule_widget.ScheduleWidget
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    val binding get() = _binding!!

    val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val newsViewModel: NewsViewModel by activityViewModels()
    private val messagesViewModel: MessagesViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

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
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.dashboardTitle.observe(viewLifecycleOwner){
            if (it.isNullOrEmpty()){
                binding.title.text = getString(R.string.app_name)
            }else{
                binding.title.text = it
            }

        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        loginViewModel.userLiveData.observe(viewLifecycleOwner){
            if (it != null){
                if (!it.profilePicPath.isNullOrEmpty()){
                    val file = File(it.profilePicPath)
                    if (file.exists()){
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.profilePic.setImageBitmap(bitmap)
                    }
                } else{
                    binding.profilePic.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.transparent))
                }
            } else {
                binding.profilePic.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.transparent))
            }
        }

        loginViewModel.authStateLiveData.observe(viewLifecycleOwner){
            messagesViewModel.updateCount()
        }

        binding.profilePic.setOnClickListener {
            ProfileDialogFragment.newInstance().show(parentFragmentManager, "ProfileDialog")
        }

        messagesViewModel.newCountTabAll.observe(viewLifecycleOwner){ it ->
            if (it != null && it > 0){
                binding.messagseCount.text = it.toString()
                binding.messagseCount.visibility = View.VISIBLE
            } else {
                binding.messagseCount.text = ""
                binding.messagseCount.visibility = View.GONE
            }
        }

        binding.messagsesButton.setOnClickListener {
            navigateTo(R.id.messagesFragment)
        }

        if (scheduleViewModel.selectedGroupLiveData.value == null) {
            scheduleViewModel.getSelectedGroup()
        }
        if (CurrentTimeObject.currentWeekLiveData.value == null) {
            scheduleViewModel.fetchCurrentWeekNumber()
        }

        val adapter = DashboardAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin)
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spacingInPixels))

        newsViewModel.fetchNews()

        scheduleViewModel.fetchCurrentWeekNumber()
        scheduleViewModel.getSelectedGroup()

        scheduleViewModel.fetchWeekLabel()

        CurrentTimeObject.weekLabelLiveData.observe(viewLifecycleOwner){
            val locale = Locale.getDefault()
            val dateFormat = SimpleDateFormat("d MMMM", locale)
            val currentDate = dateFormat.format(Date())
            if (it != null){
                adapter.updateHeader(it, currentDate)
            } else {
                adapter.updateHeader("", currentDate)
            }

        }

        scheduleViewModel.selectedGroupLiveData.observe(viewLifecycleOwner) { group ->

            if (group.isNullOrEmpty()) {
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        scheduleViewModel.getScheduleLiveData(group).observe(viewLifecycleOwner) { response ->
                            if (response.responseType == ResponseType.SUCCESS && response.data != null) {
                                val schedule = response.data as Schedule
                                CurrentTimeObject.currentWeekLiveData.observe(viewLifecycleOwner){ currentWeekNumber ->
                                    val currentDayNumber = CurrentTimeObject.currentDayLiveData.value

                                    var nextDayWithLessons = schedule.findNextDayWithLessonsAfter(
                                        weekNumber = currentWeekNumber,
                                        date = CurrentTimeObject.currentDateLiveData.value!!,
                                        time = CurrentTimeObject.currentTimeLiveData.value!!
                                    )



                                    val dayNumber = nextDayWithLessons?.first ?: currentDayNumber
                                    val weekNumber = nextDayWithLessons?.second ?: currentWeekNumber

                                    if (adapter.group != group || adapter.weekNumber != weekNumber || adapter.dayNumber != dayNumber){
                                        adapter.updateScheduleData(group, weekNumber, dayNumber!!)
                                    }
                                }

                            } else {

                            }
                        }
                    }
                }

                if (scheduleViewModel.getScheduleLiveData(group).value?.data == null) {
                    scheduleViewModel.fetchSchedule(group)
                }
            }
        }

        newsViewModel.newsListLiveData.observe(viewLifecycleOwner){
            if (it != null) {
                adapter.updateNews(it)
            }
        }


    }

    fun openGroupSearchDialog(){
        SearchGroupBottomSheet().show(
            requireActivity().supportFragmentManager,
            SearchGroupBottomSheet.TAG
        )
    }

    fun openDialogForLesson() {
        val modalBottomSheet = LessonViewBottomSheetFragment()
        modalBottomSheet.show(requireActivity().supportFragmentManager, LessonViewBottomSheetFragment.TAG)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
