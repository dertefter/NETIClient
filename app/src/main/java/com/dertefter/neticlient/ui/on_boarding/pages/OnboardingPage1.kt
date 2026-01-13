package com.dertefter.neticlient.ui.on_boarding.pages

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentOnboardingPage1Binding
import kotlin.random.Random

class OnboardingPage1 : Fragment() {
    private var _binding: FragmentOnboardingPage1Binding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 2000L

    private val ids = listOf(
        R.drawable.p_000, R.drawable.p_001, R.drawable.p_002,
        R.drawable.p_003, R.drawable.p_004, R.drawable.p_005,
        R.drawable.p_006, R.drawable.p_007, R.drawable.p_008,
        R.drawable.p_009, R.drawable.p_010, R.drawable.p_011,
        R.drawable.p_012
    )

    private val imageUpdater = object : Runnable {
        override fun run() {
            val randomId = ids.random()
            binding.sv.setDrawableResId(
                randomId,
                newShape = binding.sv.getRandomShape()
            )
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPage1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge(binding.appbar))
        handler.post(imageUpdater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(imageUpdater)
        _binding = null
    }
}
