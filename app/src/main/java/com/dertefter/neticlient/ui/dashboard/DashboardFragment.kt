package com.dertefter.neticlient.ui.dashboard

import android.animation.ObjectAnimator
import android.graphics.BitmapFactory
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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDashboardBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    lateinit var binding: FragmentDashboardBinding
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    var adapter: DashboardPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = DashboardPagerAdapter(this)
        binding.r.adapter = adapter

        val viewPager = binding.r

        viewPager.apply {
            clipToPadding = false   // allow full width shown with padding
            clipChildren = false    // allow left/right item is not clipped
            offscreenPageLimit = 3  // make sure left/right item is rendered
        }
        val transformer = PageTransformer(requireContext())
        viewPager.setPageTransformer(transformer)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = 0,
            )
            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.r) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }

        binding.tuneButton.setOnClickListener {
            val navOptions = Utils.basicTransitionAnimations()
            findNavController().navigate(R.id.dashboardTuneFragment, null, navOptions)

        }


        dashboardViewModel.headerLabelLiveData.observe(viewLifecycleOwner) {
            if (it.responseType == ResponseType.SUCCESS) {
                Utils.basicAnimationOn(binding.headerLabelContainer).start()
                binding.headerLabelContainer.visibility = View.VISIBLE
                binding.headerLabel.text = it.data.toString()
            } else {
                binding.headerLabelContainer.visibility = View.GONE
            }
        }

        dashboardViewModel.fetchHeaderLabel()

        dashboardViewModel.dashboardItemListLiveData.observe(viewLifecycleOwner) {
            adapter?.setData(it)
            if (it.isEmpty()){
                binding.noItems.visibility = View.VISIBLE
                Utils.basicAnimationOn(binding.noItems).start()
            } else {
                Utils.basicAnimationOff(binding.noItems).start()
            }
        }

        dashboardViewModel.fetchDashboardItemList()

        binding.settingButton.setOnClickListener {
            findNavController().navigate(
                R.id.dashboardTuneFragment,
                null,
                Utils.basicTransitionAnimations(),
            )


        }

        binding.r.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    binding.appBarLayout.visibility = View.VISIBLE
                } else {
                    binding.appBarLayout.visibility = View.GONE
                }
            }
        })
    }
}
