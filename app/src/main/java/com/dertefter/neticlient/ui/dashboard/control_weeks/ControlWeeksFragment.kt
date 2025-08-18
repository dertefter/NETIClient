package com.dertefter.neticlient.ui.dashboard.control_weeks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMoneyBinding
import com.dertefter.neticlient.ui.dashboard.sessia_results.SemestrPagerAdapter
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.control_weeks.ControlResult
import com.dertefter.neticlient.databinding.FragmentControlWeeksBinding
import com.dertefter.neticlient.ui.dashboard.sessia_results.SessiaResultsViewModel
import com.dertefter.neticlient.ui.money.MoneyPagerAdapter
import com.dertefter.neticlient.ui.money.MoneyYearsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ControlWeeksFragment : Fragment() {

    private var _binding: FragmentControlWeeksBinding? = null
    private val binding get() = _binding!!

    private val controlWeeksViewModel: ControlWeeksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlWeeksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        val adapter = ControlSemesterAdapter(emptyList())
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                controlWeeksViewModel.uiStateFlow.collect { uiState ->
                    val semestrs = (uiState.data as ControlResult?)?.items
                    if (!semestrs.isNullOrEmpty()){
                        adapter.updateSemestrs(semestrs)
                        binding.skeleton.isGone = true
                        binding.loadFail.root.isGone = true
                    }
                    if (uiState.responseType == ResponseType.LOADING && semestrs.isNullOrEmpty()){
                        binding.skeleton.isGone = false
                        binding.loadFail.root.isGone = true
                    }

                    if (uiState.responseType == ResponseType.ERROR && semestrs.isNullOrEmpty()){
                        binding.skeleton.isGone = true
                        binding.loadFail.root.isGone = false
                    }
                }
            }
        }

        controlWeeksViewModel.updateControlWeeks()

        binding.loadFail.buttonRetry.setOnClickListener {
            controlWeeksViewModel.updateControlWeeks()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



