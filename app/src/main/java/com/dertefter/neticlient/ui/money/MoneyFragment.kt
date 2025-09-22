package com.dertefter.neticlient.ui.money

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMoneyBinding
import com.dertefter.neticlient.ui.dashboard.sessia_results.SemestrPagerAdapter
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MoneyFragment : Fragment() {

    private var _binding: FragmentMoneyBinding? = null
    private val binding get() = _binding!!
    private val moneyYearsViewModel: MoneyYearsViewModel by viewModels()


    lateinit var pagerAdapter: MoneyPagerAdapter

    fun collectStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                moneyYearsViewModel.status.collect { status ->
                    when (status) {
                        com.dertefter.neticore.network.ResponseType.LOADING -> {
                            binding.refreshLayout.startRefreshing()
                        }
                        com.dertefter.neticore.network.ResponseType.SUCCESS -> {
                            binding.refreshLayout.stopRefreshing()
                        }
                        com.dertefter.neticore.network.ResponseType.ERROR -> {
                            binding.refreshLayout.showError()
                        }
                    }
                }
            }
        }
    }


    fun collectYears(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                moneyYearsViewModel.years.collect { years ->
                    pagerAdapter.setData(years ?: emptyList())
                }
            }
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        


        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))
        pagerAdapter = MoneyPagerAdapter(this)
        binding.pager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.pager){ tab, position ->
            tab.text = pagerAdapter.getItem(position)
        }.attach()

        moneyYearsViewModel.updateYearList()
        binding.refreshLayout.setOnRefreshListener {
            moneyYearsViewModel.updateYearList()
        }
        collectStatus()
        collectYears()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



