package com.dertefter.neticlient.ui.dashboard.sessia_results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.databinding.FragmentSemestrBinding
import com.dertefter.neticore.features.sessia_results.model.SessiaResultSemestr

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
        val semestr = arguments?.getParcelable<SessiaResultSemestr>("SEMESTR")
        val items = semestr?.items
        val adapter = SessiaResultAdapter(emptyList())
        binding.recyclerView.adapter = adapter



        if (items != null){
            adapter.updateData(items)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.addItemDecoration(
                VerticalSpaceItemDecoration( R.dimen.max, R.dimen.min, R.dimen.micro)
            )
        }




    }

}