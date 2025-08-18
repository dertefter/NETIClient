package com.dertefter.neticlient.ui.on_boarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.FragmentOnBoardingBinding
import com.dertefter.neticlient.ui.main.MainActivity
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class OnBoardingFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.setOnBoarding(false)

        val adapter = OnboardingPagerAdapter(this)

        binding.pager.offscreenPageLimit = 5
        binding.pager.adapter = adapter
        binding.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        ViewCompat.setOnApplyWindowInsetsListener(binding.next) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val defaultMargin = resources.getDimensionPixelSize(R.dimen.margin)
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = (defaultMargin + insets.bottom).toInt()
            layoutParams.rightMargin = (defaultMargin).toInt()
            v.layoutParams = layoutParams

            WindowInsetsCompat.CONSUMED
        }



        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position==0){
                    binding.next.setOnClickListener {
                        binding.pager.currentItem =  binding.pager.currentItem + 1
                    }
                    binding.next.text = getString(R.string.on_board_fab_next)
                    binding.next.extend()
                } else if (position ==adapter.itemCount - 1){
                    binding.next.setOnClickListener {
                        (requireActivity() as MainActivity).cancelOnBoarding()
                    }
                    binding.next.text =  getString(R.string.on_board_fab_done)
                    binding.next.extend()
                }
                else{
                    binding.next.setOnClickListener {
                        binding.pager.currentItem =  binding.pager.currentItem + 1
                    }
                    binding.next.shrink()
                }

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

    }

}



