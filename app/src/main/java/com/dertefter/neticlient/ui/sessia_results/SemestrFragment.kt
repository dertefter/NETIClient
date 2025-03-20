package com.dertefter.neticlient.ui.sessia_results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.databinding.FragmentSemestrBinding
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration

class SemestrFragment : Fragment() {

    lateinit var binding: FragmentSemestrBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSemestrBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val semestr = arguments?.getParcelable<SessiaResultSemestr>("SEMESTR")!!
        val items = semestr.items
        binding.recyclerView.adapter = SessiaResultAdapter(items)

        val spanCount = resources.getInteger(R.integer.span_count)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        val decoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                resources.getInteger(R.integer.span_count),
                R.dimen.margin, R.dimen.margin_min
            )
        )

    }

}