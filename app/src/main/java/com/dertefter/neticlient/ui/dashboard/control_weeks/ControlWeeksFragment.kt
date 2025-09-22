package com.dertefter.neticlient.ui.dashboard.control_weeks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticore.features.control_weeks.model.ControlResult
import com.dertefter.neticlient.databinding.FragmentControlWeeksBinding
import com.dertefter.neticore.network.ResponseType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ControlWeeksFragment : Fragment() {

    private var _binding: FragmentControlWeeksBinding? = null
    private val binding get() = _binding!!
    private val controlWeeksViewModel: ControlWeeksViewModel by viewModels()

    lateinit var adapter: ControlSemesterAdapter

    fun collectStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                controlWeeksViewModel.status.collect { status ->
                    binding.skeleton.isGone = status != ResponseType.LOADING
                    binding.loadFail.root.isGone = status != ResponseType.ERROR
                    binding.recyclerview.isGone = status != ResponseType.SUCCESS
                }
            }
        }
    }


    fun collectControlWeeks(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                controlWeeksViewModel.controlWeeks.collect { controlWeeks ->
                    val semesters = controlWeeks?.items
                    if (semesters != null){
                        adapter.updateSemestrs(semesters)
                    }
                }
            }
        }
    }

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

        adapter = ControlSemesterAdapter(emptyList())
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        collectStatus()
        collectControlWeeks()

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



