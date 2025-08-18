package com.dertefter.neticlient.ui.schedule.lesson_view

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.data.model.schedule.LessonDetail
import com.dertefter.neticlient.data.model.schedule.Week
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
        val adapter = PersonListRecyclerViewAdapter(fragment = this, listStyle = PersonListStyle.LIST){
            val bundle = Bundle()
            bundle.putString("personId", it)
            requireActivity().findNavController(R.id.nav_host_container).navigate(R.id.personViewFragment, bundle)
            dismiss()
        }

        val lessonDetail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("lessonDetail", LessonDetail::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("lessonDetail") as? LessonDetail
        }

        if (lessonDetail != null){
            val lesson = lessonDetail.lesson
            val time = lessonDetail.time
            binding.aud.text = lesson.aud
            binding.title.text = lesson.title
            binding.type.text = lesson.type
            binding.timeEnd.text = time?.timeEnd.toString()
            binding.timeStart.text = time?.timeStart.toString()

            binding.personsRecyclerView.adapter = adapter
            binding.personsRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(R.dimen.margin_min))

            binding.personsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter.setData(lesson.personIds)
            when (lessonDetail.futureOrPastOrNow){
                FutureOrPastOrNow.NOW -> {
                    if (time != null){
                        binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorSecondary))
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
                    binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorSecondary))
                    binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorSecondary))
                    binding.progress.progress = 100
                    (binding.progress.parent as View).alpha = 0.5f
                }
            }

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

    }

}



