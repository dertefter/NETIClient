package com.dertefter.neticlient.ui.schedule.lesson_view

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticore.features.schedule.model.FutureOrPastOrNow
import com.dertefter.neticore.features.schedule.model.LessonDetail
import com.dertefter.neticlient.databinding.FragmentLessonViewBinding
import com.dertefter.neticlient.ui.person.PersonListAdapter
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.dertefter.neticore.features.schedule.model.Lesson
import com.dertefter.neticore.features.schedule.model.Time
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class LessonViewBottomSheetFragment : BottomSheetDialogFragment() {


    companion object {
        const val TAG = "LessonDetailBottomSheetFragment"
    }

    private var _binding: FragmentLessonViewBinding? = null
    private val binding get() = _binding!!

    lateinit var _dialog: BottomSheetDialog

    lateinit var lesson: Lesson

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        _dialog = dialog
        return dialog
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonViewBinding.inflate(inflater, container, false)
        return binding.root
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lesson = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("lesson", Lesson::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("lesson") as? Lesson
        })!!

        binding.aud.text = lesson.aud
        binding.title.text = lesson.title
        binding.type.text = lesson.type
        binding.timeEnd.text = lesson.timeEnd.toString()
        binding.timeStart.text = lesson.timeStart.toString()


        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                CurrentTimeObject.currentTimeFlow,
                CurrentTimeObject.currentDateFlow
            ) { currentTime, currentDate ->
                Pair(currentTime, currentDate)
            }.collect { (currentTime, currentDate) ->
                if (currentTime != null && currentDate != null) {
                    updateProgressBasedOnTime(currentTime, currentDate)
                }
            }
        }


        val adapter = PersonListAdapter(lesson.personIds, viewLifecycleOwner){
            val bundle = Bundle()
            bundle.putString("personId", it)
            requireActivity().findNavController(R.id.nav_host_container).navigate(R.id.personViewFragment, bundle)
            dismiss()
        }


        binding.personsRecyclerView.adapter = adapter
        binding.personsRecyclerView.addItemDecoration(
            VerticalSpaceItemDecoration(
                R.dimen.margin_max,
                R.dimen.margin_micro
            ))
        binding.personsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        if (binding.aud.text.isEmpty()){
            binding.aud.visibility = View.GONE
        } else {
            binding.aud.visibility = View.VISIBLE
        }

        val type = binding.type

        if (type.text.isNotEmpty()) {
            type.visibility = View.VISIBLE
            when {
                type.text.contains("Лаб") -> {
                    type.backgroundTintList = ColorStateList.valueOf(
                        MaterialColors.getColor(type, com.google.android.material.R.attr.colorSecondaryContainer)
                    )
                    type.setTextColor(
                        MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnSecondaryContainer)
                    )
                }
                type.text.contains("Практика") -> {
                    type.backgroundTintList = ColorStateList.valueOf(
                        MaterialColors.getColor(type, com.google.android.material.R.attr.colorPrimaryContainer)
                    )
                    type.setTextColor(
                        MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnPrimaryContainer)
                    )
                }
                else -> {
                    type.backgroundTintList = ColorStateList.valueOf(
                        MaterialColors.getColor(type, com.google.android.material.R.attr.colorTertiaryContainer)
                    )
                    type.setTextColor(
                        MaterialColors.getColor(type, com.google.android.material.R.attr.colorOnTertiaryContainer)
                    )


                }
            }
        } else {
            type.visibility = View.GONE
        }


    }


    private fun updateProgressBasedOnTime(
        currentTime: LocalTime,
        currentDate: LocalDate
    ) {
        try {
            val startTime = lesson.getTimeStart()
            val endTime =  lesson.getTimeEnd()

            if (currentDate.isBefore(lesson.getLocalDate())){
                onFuture()
            } else if (currentDate.isAfter(lesson.getLocalDate())){
                onPast()
            }else{
                when {
                    currentTime.isBefore(startTime) -> {
                        onFuture()
                    }
                    currentTime.isAfter(endTime) -> {
                        onPast()
                    }
                    else -> {
                        val progress = calculateProgress(startTime, endTime, currentTime)
                        onNow(progress)
                    }
                }
            }


        } catch (e: Exception) {
            onFuture()
            Log.e("updateProgressBasedOnTime", e.stackTraceToString())

        }
    }


    fun getDateString(date: LocalDate): String{
         when (date) {
            LocalDate.now() -> {
                return getString(R.string.today)
            }

            LocalDate.now().plusDays(1) -> {
                return getString(R.string.tomorrow)
            }

             LocalDate.now().minusDays(1) -> {
                 return getString(R.string.yesterday)
             }

            else -> {
                val formatter = DateTimeFormatter.ofPattern("d MMMM")
                val dateString = date.format(formatter)
                return dateString
            }
        }

    }

    fun onFuture(){
        binding.futurePast.isGone = false
        binding.now.isGone = true
        binding.timeStartFuture.text = lesson.timeStart
        binding.dateTv.text = getDateString(date = lesson.getLocalDate()).replaceFirstChar { it.uppercase() }
        binding.futurePastTv.text = getString(R.string.will_start)
    }

    fun onPast(){
        binding.futurePast.isGone = false
        binding.now.isGone = true
        binding.timeStartFuture.text = lesson.timeEnd
        binding.dateTv.text = getDateString(date = lesson.getLocalDate()).replaceFirstChar { it.uppercase() }
        binding.futurePastTv.text = getString(R.string.lesson_over)
    }

    fun onNow(progress: Int){
        binding.futurePast.isGone = true
        binding.now.isGone = false
        binding.progress.progress = progress
    }

    private fun calculateProgress(
        startTime: LocalTime?,
        endTime: LocalTime?,
        currentTime: LocalTime?
    ): Int {

        if (startTime == null || endTime == null || currentTime == null) {return 0}

        val totalMinutes = startTime.until(endTime, java.time.temporal.ChronoUnit.MINUTES)
        val elapsedMinutes = startTime.until(currentTime, java.time.temporal.ChronoUnit.MINUTES)

        return if (totalMinutes > 0) {
            ((elapsedMinutes.toDouble() / totalMinutes.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }


}



