package com.dertefter.neticlient.ui.on_boarding.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentOnboardingPage1Binding

class OnboardingPage1 : Fragment() {
    private var _binding: FragmentOnboardingPage1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPage1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.sv.setDrawableResIds(
            listOf(
                R.drawable.p_000, R.drawable.p_001, R.drawable.p_002, R.drawable.p_003,
                R.drawable.p_004, R.drawable.p_005, R.drawable.p_006, R.drawable.p_007,
                R.drawable.p_008, R.drawable.p_009, R.drawable.p_010, R.drawable.p_011, R.drawable.p_012,

            )
        )
        binding.sv.startAutoMorphing()

        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))


        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
