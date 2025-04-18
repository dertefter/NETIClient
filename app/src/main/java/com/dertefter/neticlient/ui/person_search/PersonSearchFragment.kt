package com.dertefter.neticlient.ui.person_search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentPersonSearchBinding
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonSearchFragment : Fragment() {

    lateinit var binding: FragmentPersonSearchBinding
    private val personSearchViewModel: PersonSearchViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonSearchBinding.inflate(inflater, container, false)

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

        binding.searchField.editText?.doOnTextChanged { text, start, before, count ->
            personSearchViewModel.fetchPersonSearchResults(text.toString())
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        val decoration = GridSpacingItemDecoration(requireContext(), 1, R.dimen.margin_min)
        binding.recyclerView.addItemDecoration(decoration)

        val adapter = PersonSearchRecyclerViewAdapter(
            fragment = this, listStyle = PersonListStyle.LIST,){ personId ->
            openPerson(personId)

        }
        binding.recyclerView.adapter = adapter

        personSearchViewModel.personIdListLiveData.observe(viewLifecycleOwner){
            Log.e("personSearchViewModel", it.toString())
            if (it.responseType == ResponseType.SUCCESS){
                if (it.data != null){
                    adapter.setData(it.data as List<Pair<String, String>>)
                    if ((it.data as List<Pair<String, String>>).isEmpty()){
                        binding.infoTextView.visibility = View.VISIBLE
                        if (binding.searchField.editText?.text.isNullOrEmpty()){
                            binding.infoTextView.text = getString(R.string.search_person_hint)
                        }
                        binding.infoTextView.text = getString(R.string.search_empty)
                    }else{
                        binding.infoTextView.visibility = View.GONE

                    }

                } else {
                    binding.infoTextView.visibility = View.VISIBLE
                    binding.infoTextView.text = getString(R.string.load_fail)
                }
            } else if (it.responseType == ResponseType.ERROR) {
                binding.infoTextView.visibility = View.VISIBLE
                binding.infoTextView.text = getString(R.string.load_fail)
            }
        }

    }

    fun openPerson(personId: String){
        val bundle = Bundle()
        bundle.putString("personId", personId)
        findNavController().navigate(R.id.personViewFragment, bundle)
    }

}