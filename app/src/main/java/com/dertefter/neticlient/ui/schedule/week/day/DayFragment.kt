package com.dertefter.neticlient.ui.schedule.week.day

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticore.features.schedule.model.Day
import com.dertefter.neticlient.databinding.FragmentDayBinding
import com.dertefter.neticlient.ui.schedule.lesson_view.LessonViewBottomSheetFragment
import com.dertefter.neticlient.ui.settings.SettingsViewModel

class DayFragment : Fragment() {

    lateinit var binding: FragmentDayBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val day = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("DAY", Day::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("DAY") as? Day
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val adapter = TimesAdapter(
            emptyList(), viewLifecycleOwner,
            onLessonClick = { lesson ->
                val bottomSheet = LessonViewBottomSheetFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("lesson", lesson)
                    }
                }
                bottomSheet.show(parentFragmentManager, "LessonDetail")
            },
            onCurrentTimeSlotFound = {},
            onLatestPastTimeSlotFound = { },
            onFirstFutureTimeSlotFound = {},
        )
        binding.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager

        binding.recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration(
                R.dimen.margin_max,
                R.dimen.margin_micro
            )
        )

        if (day != null){
            adapter.updateData(day.times)
            if (day.times.isEmpty()){
                binding.noLessons.visibility = View.VISIBLE
            }else {
                binding.noLessons.visibility = View.GONE
            }
        }

    }

    fun setVisibleNoLessons(b: Boolean){
        binding.noLessons.visibility = if (b) View.VISIBLE else View.GONE
    }

}