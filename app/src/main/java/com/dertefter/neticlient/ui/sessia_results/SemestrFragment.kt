package com.dertefter.neticlient.ui.sessia_results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val spanCount = resources.getInteger(R.integer.span_count)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        if (spanCount == 1){
            binding.recyclerView.addItemDecoration(
                GridSpacingItemDecoration(
                    requireContext(),
                    resources.getInteger(R.integer.span_count),
                    R.dimen.margin, R.dimen.margin_min
                )
            )
        }else{
            binding.recyclerView.addItemDecoration(
                GridSpacingItemDecoration(
                    requireContext(),
                    resources.getInteger(R.integer.span_count),
                    R.dimen.margin, R.dimen.margin
                )
            )
        }


    }

}