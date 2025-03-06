package com.dertefter.neticlient.ui.schedule.lesson_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.dertefter.neticlient.databinding.FragmentLessonViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LessonViewBottomSheetFragment : BottomSheetDialogFragment() {


    companion object {
        const val TAG = "LessonViewBottomSheetFragment"
    }

    private var _binding: FragmentLessonViewBinding? = null
    private val binding get() = _binding!!

    private val lessonViewViewModel: LessonViewViewModel by activityViewModels()





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonViewBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lessonViewViewModel.lessonLiveData.observe(viewLifecycleOwner){ lesson ->
            val time = lessonViewViewModel.timeLiveData.value
            if (lesson != null && time != null){
                binding.aud.text = lesson.aud
                binding.title.text = lesson.title
                binding.type.text = lesson.type
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



