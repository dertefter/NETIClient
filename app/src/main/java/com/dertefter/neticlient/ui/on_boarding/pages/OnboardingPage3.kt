package com.dertefter.neticlient.ui.on_boarding.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentOnboardingPage3Binding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.search_group.SearchGroupBottomSheet
import kotlinx.coroutines.launch

class OnboardingPage3 : Fragment() {
    private var _binding: FragmentOnboardingPage3Binding? = null
    private val binding get() = _binding!!


    val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPage3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))
        binding.searchGroupButton2.setOnClickListener {
            openGroupSearchDialog()
        }
        binding.searchGroupButton.setOnClickListener {
            openGroupSearchDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.scheduleState.collect { state ->
                    val group = state.group
                    binding.noGroup.isGone = !group.isNullOrEmpty()
                    binding.group.isGone = group.isNullOrEmpty()
                    binding.groupView.text = group
                }
            }
        }


        }

    fun openGroupSearchDialog(){
        SearchGroupBottomSheet().show(
            requireActivity().supportFragmentManager,
            SearchGroupBottomSheet.TAG
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
