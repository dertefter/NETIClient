package com.dertefter.neticlient.ui.sessia_results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultSemestr
import com.dertefter.neticlient.databinding.FragmentSemestrBinding

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
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val decoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        decoration.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_divider, null)!!)
        binding.recyclerView.addItemDecoration(decoration)

    }

}