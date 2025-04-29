package com.dertefter.neticlient.ui.sessia_results

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentSessiaResultsBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SessiaResultsFragment : Fragment() {

    private var _binding: FragmentSessiaResultsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val sessiaResultsViewModel: SessiaResultsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessiaResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.setLiftable(true)
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.toolbar, false).start()
                binding.appBarLayout.isLifted = true
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
                binding.appBarLayout.isLifted = false
            }
        }

        sessiaResultsViewModel.sessiaResultsLiveData.observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                val pagerAdapter = SemestrPagerAdapter(this)
                pagerAdapter.setData(it.data as List<SessiaResultSemestr>)
                binding.pager.adapter = pagerAdapter
                binding.pager.offscreenPageLimit = 8
                TabLayoutMediator(binding.tabLayout, binding.pager){ tab, position ->
                    tab.text = "${it.data[position].title} семестр"
                }.attach()
            }
        }

        sessiaResultsViewModel.fetchResponseResults()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}



