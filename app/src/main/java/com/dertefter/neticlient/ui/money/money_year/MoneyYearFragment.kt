package com.dertefter.neticlient.ui.money.money_year

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticore.features.money.model.MoneyItem
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMoneyYearBinding
import com.dertefter.neticlient.databinding.FragmentSemestrBinding
import com.dertefter.neticlient.ui.money.MoneyRecyclerViewAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoneyYearFragment : Fragment() {

    lateinit var binding: FragmentMoneyYearBinding
    private val moneyItemsViewModel: MoneyItemsViewModel by viewModels()

    lateinit var adapter: MoneyRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoneyYearBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun collectMoneyItems(year: String){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                moneyItemsViewModel.moneyItemsForYear(year).collect { moneyItems ->

                    if (moneyItems == null){
                        adapter.submitList(emptyList())
                    }else{
                        adapter.submitList(moneyItems)
                    }

                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val defaultMargin = resources.getDimensionPixelSize(R.dimen.maximorum)
            v.updatePadding(
                bottom = defaultMargin + insets.bottom,
                left = defaultMargin + insets.left,
                right = defaultMargin + insets.right
            )

            WindowInsetsCompat.CONSUMED
        }


        val year = arguments?.getString("YEAR")!!

        adapter = MoneyRecyclerViewAdapter()

        binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val decoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        decoration.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_divider_big, null)!!)
        binding.recyclerView.addItemDecoration(decoration)

        collectMoneyItems(year)
        moneyItemsViewModel.updateMoneyItemsForYear(year)
    }

}