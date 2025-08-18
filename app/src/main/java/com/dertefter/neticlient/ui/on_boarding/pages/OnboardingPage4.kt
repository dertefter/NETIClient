package com.dertefter.neticlient.ui.on_boarding.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentOnboardingPage1Binding
import com.dertefter.neticlient.databinding.FragmentOnboardingPage4Binding

class OnboardingPage4 : Fragment() {
    private var _binding: FragmentOnboardingPage4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPage4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))


        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
