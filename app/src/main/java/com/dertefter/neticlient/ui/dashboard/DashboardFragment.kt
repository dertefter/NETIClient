package com.dertefter.neticlient.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.sessia_results.SessiaResults
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDashboardBinding
import com.dertefter.neticlient.ui.dashboard.sessia_results.SemestrPagerAdapter
import com.dertefter.neticlient.ui.dashboard.sessia_results.SessiaResultsViewModel
import com.dertefter.neticlient.ui.dashboard.share_score_bottom_sheet.ShareScoreSheetFragment
import com.dertefter.neticlient.ui.login.LoginFragment
import com.dertefter.neticlient.ui.login.LoginReasonType
import com.dertefter.neticlient.ui.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val sessiaResultsViewModel: SessiaResultsViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()


    lateinit var pagerAdapter: SemestrPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentDashboardBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pagerAdapter = SemestrPagerAdapter(this)
        binding.apply {
            pager.adapter = pagerAdapter
            pager.offscreenPageLimit = 3
            appbar.addOnOffsetChangedListener(AppBarEdgeToEdge(appbar))

            binding.menuButton.setOnClickListener { view ->
                val popup = PopupMenu(requireContext(), view)
                popup.menuInflater.inflate(R.menu.dashboard_appbar_menu, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.controlWeeks -> {
                            navigateTo(R.id.controlWeeksFragment)
                            true
                        }
                        R.id.shareScore -> {
                            ShareScoreSheetFragment().show(parentFragmentManager, "ShareScore")
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }


        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.chart?.setSelectedPosition(position)
                binding.horizChart?.setSelectedPosition(position)
                val item = pagerAdapter.getItem(position)
                binding.toolbar.title = getString(R.string.semester) + " " + item.title

                if (item.srScoreSem != null && item.srScoreSem != 0f){
                    val rounded = String.format("%.2f", item.srScoreSem)
                    binding.toolbar.subtitle = getString(R.string.sr_ball) + ": " + rounded
                }

            }
        })


        binding.chart?.onBarSelectedListener = { it, position ->

            binding.pager.currentItem = position
        }
        binding.horizChart?.onBarSelectedListener = { it, position ->

            binding.pager.currentItem = position
        }

        collectAuthState()
        collectSessiaResults()
        sessiaResultsViewModel.updateSessiaResults()
    }

    private fun navigateTo(destination: Int) {
        findNavController().navigate(destination, null, Utils.getNavOptions())
    }

    private fun collectAuthState() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            loginViewModel.authStateFlow.collect { authState ->
                binding.apply {
                    val unauthorized = authState == AuthState.UNAUTHORIZED
                    buttons.isGone = unauthorized
                    pager.isGone = unauthorized

                    if (unauthorized){
                        loginHelper.isGone = false
                        childFragmentManager.beginTransaction().replace(loginHelper.id, LoginFragment(
                            LoginReasonType.UNAUTHORIZED)
                        ).commit()
                    }else{
                        loginHelper.isGone = true
                    }
                }
            }
        }
    }

    private fun collectSessiaResults() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            sessiaResultsViewModel.uiStateFlow.collect { uiState ->
                val results = uiState.data as SessiaResults?
                when (uiState.responseType) {
                    ResponseType.LOADING -> {
                        if (results == null) {
                            binding.skeleton.isGone = false
                            binding.emptyView.isGone = true
                            binding.chart?.isGone = true
                            binding.horizChart?.isGone = true
                        }
                    }
                    ResponseType.SUCCESS -> {
                        binding.skeleton.isGone = true
                        val hasData = !results?.semestrs.isNullOrEmpty()
                        binding.emptyView.isGone = hasData



                        if (hasData) {
                            binding.chart?.isGone = false
                            binding.horizChart?.isGone = false
                            pagerAdapter.setData(results!!.semestrs)
                            binding.chart?.data = results!!.semestrs
                            binding.horizChart?.data = results!!.semestrs
                        }




                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
