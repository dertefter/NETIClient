package com.dertefter.neticlient.ui.dashboard

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDashboardBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.messages.MessagesViewModel
import com.dertefter.neticlient.ui.news.NewsViewModel
import com.dertefter.neticlient.ui.profile.profile_dialog.ProfileDialogFragment
import com.dertefter.neticlient.ui.schedule.ScheduleUiState
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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

    private lateinit var adapter: DashboardAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DashboardAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


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


        CurrentTimeObject.currentTimeLiveData.observe(viewLifecycleOwner){ time ->
            val date = CurrentTimeObject.currentDateLiveData.value
            if (date != null) {
                adapter.updateTimeAndDate(date, time)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.scheduleState.collect { state ->
                    adapter.updateScheduleState(state)
                    Log.e("ssssss", state.toString())

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
                    Log.e("updateHeader i nfr", "$weekLabel $currentDate")
                    if (weekLabel != null){
                        adapter.updateHeader(weekLabel, currentDate)
                    } else {
                        adapter.updateHeader("", currentDate)
                    }
                }
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


        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin)
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spacingInPixels))

        newsViewModel.fetchNews()

        scheduleViewModel.updateWeekLabel()

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


}
