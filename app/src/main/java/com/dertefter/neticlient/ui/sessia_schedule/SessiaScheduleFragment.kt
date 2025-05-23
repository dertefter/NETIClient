package com.dertefter.neticlient.ui.sessia_schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentSessiaScheduleBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import kotlinx.coroutines.launch

class SessiaScheduleFragment : Fragment() {

    lateinit var binding: FragmentSessiaScheduleBinding
    lateinit var adapter: SessiaScheduleRecyclerViewAdapter

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSessiaScheduleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SessiaScheduleRecyclerViewAdapter(emptyList(), this)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin)
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spacingInPixels))
        adapter.setLoading(true)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.recyclerView.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.scheduleSessiaState.collect { state ->
                    when (state.responseType) {
                        ResponseType.LOADING -> {
                            adapter.setLoading(true)
                        }
                        ResponseType.SUCCESS -> {
                            adapter.setLoading(false)
                            adapter.updateList(state.schedule.orEmpty())
                        }
                        ResponseType.ERROR -> {}
                    }
                }
            }
        }

    }
}