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
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMoneyBinding
import com.dertefter.neticlient.ui.sessia_results.SemestrPagerAdapter
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MoneyFragment : Fragment() {

    private var _binding: FragmentMoneyBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val moneyYearsViewModel: MoneyYearsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner){
            binding.appBarLayout.updatePadding(
                top = it[0],
                bottom = 0,
                right = it[2],
                left = it[3]
            )
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.toolbar, false).start()
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
            }
        }

        moneyYearsViewModel.yearListLiveData.observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                val pagerAdapter = MoneyPagerAdapter(this)
                pagerAdapter.setData(it.data as List<String>)
                binding.pager.adapter = pagerAdapter
                binding.pager.offscreenPageLimit = 8
                TabLayoutMediator(binding.tabLayout, binding.pager){ tab, position ->
                    tab.text = it.data[position]
                }.attach()
            }
        }

        moneyYearsViewModel.fetchYearList()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



