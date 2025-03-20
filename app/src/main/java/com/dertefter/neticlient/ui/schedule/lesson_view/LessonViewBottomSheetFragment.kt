package com.dertefter.neticlient.ui.schedule.lesson_view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.AvatarOverlapItemDecoration
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.databinding.FragmentLessonViewBinding
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LessonViewBottomSheetFragment : BottomSheetDialogFragment() {


    companion object {
        const val TAG = "LessonDetailBottomSheetFragment"
    }

    private var _binding: FragmentLessonViewBinding? = null
    private val binding get() = _binding!!

    private val lessonViewViewModel: LessonDetailViewModel by activityViewModels()
    lateinit var _dialog: BottomSheetDialog


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
        val adapter = PersonListRecyclerViewAdapter(fragment = this, listStyle = PersonListStyle.CARDS){
            val bundle = Bundle()
            bundle.putString("personId", it)
            requireActivity().findNavController(R.id.nav_host).navigate(R.id.personViewFragment, bundle)
            dismiss()
        }
        lessonViewViewModel.lessonDetailLiveData.observe(viewLifecycleOwner){ lessonDetail ->

            if (lessonDetail != null){
                val lesson = lessonDetail.lesson
                val time = lessonDetail.time
                binding.aud.text = lesson.aud
                binding.title.text = lesson.title
                binding.type.text = lesson.type
                binding.timeEnd.text = time?.timeEnd.toString()
                binding.timeStart.text = time?.timeStart.toString()

                binding.personsRecyclerView.adapter = adapter
                binding.personsRecyclerView.addItemDecoration(GridSpacingItemDecoration(requireContext(), 1, R.dimen.margin_min))

                binding.personsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter.setData(lesson.personIds)
                when (lessonDetail.futureOrPastOrNow){
                    FutureOrPastOrNow.NOW -> {
                        if (time != null){
                            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorPrimary))
                            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorOnSurfaceVariant))

                            val currentTime = CurrentTimeObject.currentTimeLiveData.value
                            val totalDuration = time.getTimeEnd().toSecondOfDay() - time.getTimeStart().toSecondOfDay()
                            val currentProgress = currentTime!!.toSecondOfDay() - time.getTimeStart().toSecondOfDay()
                            val progressPercentage = (currentProgress.toFloat() / totalDuration.toFloat()) * 100
                            binding.progress.progress = progressPercentage.toInt()
                        }
                    }
                    FutureOrPastOrNow.FUTURE -> {
                        binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorOnSurfaceVariant))
                        binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorOnSurfaceVariant))
                        binding.progress.progress = 0
                    }
                    FutureOrPastOrNow.PAST -> {
                        binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorPrimary))
                        binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorPrimary))
                        binding.progress.progress = 100
                    }
                }





            } else {
                dismiss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



