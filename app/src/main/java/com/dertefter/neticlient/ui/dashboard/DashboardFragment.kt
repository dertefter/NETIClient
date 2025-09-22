package com.dertefter.neticlient.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
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
import com.dertefter.neticlient.databinding.FragmentDashboardBinding
import com.dertefter.neticlient.ui.dashboard.sessia_results.SemestrPagerAdapter
import com.dertefter.neticlient.ui.dashboard.sessia_results.SessiaResultsViewModel
import com.dertefter.neticlient.ui.dashboard.share_score_bottom_sheet.ShareScoreSheetFragment
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticore.network.ResponseType
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
                binding.chart.setSelectedPosition(position)
                val item = pagerAdapter.getItem(position)
                binding.toolbar.title = getString(R.string.semester) + " " + item.title

                if (item.srScoreSem != null && item.srScoreSem != 0f){
                    val rounded = String.format("%.2f", item.srScoreSem)
                    binding.toolbar.subtitle = getString(R.string.sr_ball) + ": " + rounded
                }

            }
        })


        binding.chart.onBarSelectedListener = { it, position ->

            binding.pager.currentItem = position
        }

        if (binding.pager.orientation == ViewPager2.ORIENTATION_VERTICAL){
            binding.pager.isUserInputEnabled = false
            ViewCompat.setOnApplyWindowInsetsListener(binding.pager) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    right = bars.right,
                )
                WindowInsetsCompat.CONSUMED
            }
        }
        collectStatus()
        binding.refreshLayout.setOnRefreshListener {
            sessiaResultsViewModel.updateSessiaResults()
        }
        collectSessiaResults()
        sessiaResultsViewModel.updateSessiaResults()
    }

    private fun navigateTo(destination: Int) {
        findNavController().navigate(destination, null, Utils.getNavOptions())
    }


    private fun collectSessiaResults() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            sessiaResultsViewModel.sessiaResults.collect { sessiaResults ->

                val hasData = !sessiaResults?.semestrs.isNullOrEmpty()
                binding.emptyView.isGone = hasData
                binding.chart.isGone = !hasData

                binding.toolbar.isGone = !hasData
                if (hasData) {
                    binding.pager.isVisible = true
                    pagerAdapter.setData(sessiaResults.semestrs!!)
                    binding.chart.data = sessiaResults.semestrs!!
                } else {
                    binding.pager.isInvisible = true
                }

            }
        }
    }

    private fun collectStatus() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            sessiaResultsViewModel.status.collect { status ->
                when (status) {
                    ResponseType.LOADING -> {
                        binding.refreshLayout.startRefreshing()
                    }
                    ResponseType.SUCCESS -> {
                        binding.refreshLayout.stopRefreshing()
                    }
                    ResponseType.ERROR -> {
                        binding.refreshLayout.showError()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
