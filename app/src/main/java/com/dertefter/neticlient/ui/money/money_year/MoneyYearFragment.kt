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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.money.MoneyItem
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMoneyYearBinding
import com.dertefter.neticlient.databinding.FragmentSemestrBinding
import com.dertefter.neticlient.ui.money.MoneyRecyclerViewAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoneyYearFragment : Fragment() {

    lateinit var binding: FragmentMoneyYearBinding
    private val moneyItemsViewModel: MoneyItemsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoneyYearBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val year = arguments?.getString("YEAR")!!
        Log.e("year", year)
        moneyItemsViewModel.fetchMoneyItemsForYear(year)

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        moneyItemsViewModel.moneyItemsLiveData.observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                binding.recyclerView.adapter = MoneyRecyclerViewAdapter((it.data as List<MoneyItem>))
            }
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val decoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        decoration.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_divider, null)!!)
        binding.recyclerView.addItemDecoration(decoration)

    }

}