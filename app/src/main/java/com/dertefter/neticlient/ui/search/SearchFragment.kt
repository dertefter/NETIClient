package com.dertefter.neticlient.ui.search

import androidx.core.widget.addTextChangedListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentSearchBinding
import com.dertefter.neticlient.ui.person_search.PersonSearchViewModel
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.search_group.SearchGroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var currentGroupList: List<String> = emptyList()
    private var currentPersonList: List<kotlin.Pair<String, String>> = emptyList()
    private var currentNavResults: List<NavSearchViewModel.NamedDestination> = emptyList()

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    lateinit var binding: FragmentSearchBinding

    val navSearchViewModel: NavSearchViewModel by viewModels()

    private val personSearchViewModel: PersonSearchViewModel by viewModels()

    private val searchGroupViewModel: SearchGroupViewModel by activityViewModels()

    lateinit var adapter: SearchAdapter

    private fun updateCombinedSearchResults() {
        val combined = buildList {
            currentGroupList.take(12).forEach { add(SearchItem.GroupItem(it)) }
            currentPersonList.forEach { add(SearchItem.PersonItem(it.second, it.first)) }
            currentNavResults.forEach { add(SearchItem.NavDestinationItem(it)) }
        }
        adapter.submitList(combined)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }


        val navController = findNavController()

        navSearchViewModel.navGraph = (
            findNavController().navInflater.inflate(R.navigation.nav_graph_search)
        )

        adapter = SearchAdapter()

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        adapter.onNavClick = {
            findNavController().navigate(
                it.dist.id,
                null,
                Utils.getNavOptions(),
            )
        }

        adapter.onGroupClick = {
            scheduleViewModel.setCurrentGroup(it.name)
            navController.popBackStack()
        }

        adapter.onPersonClick = {
            val bundle = Bundle()
            bundle.putString("personId", it.id)
            findNavController().navigate(R.id.personViewFragment, bundle)
        }


        searchGroupViewModel.groupListLiveData.observe(viewLifecycleOwner) {
            if (it.responseType == ResponseType.SUCCESS) {
                currentGroupList = it.data as? List<String> ?: emptyList()
                updateCombinedSearchResults()
            }
        }

        personSearchViewModel.personIdListLiveData.observe(viewLifecycleOwner) {
            if (it.responseType == ResponseType.SUCCESS) {
                currentPersonList = it.data as? List<kotlin.Pair<String, String>> ?: emptyList()
                updateCombinedSearchResults()
            }
        }

        navSearchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            currentNavResults = results
            updateCombinedSearchResults()
        }

        binding.searchBar.editText?.addTextChangedListener {
            binding.emptyView.isGone = !it.toString().isEmpty()
            binding.recyclerView.isGone = it.toString().isEmpty()
            navSearchViewModel.search(it.toString())
            searchGroupViewModel.fetchGroupList(it.toString())
            personSearchViewModel.fetchPersonSearchResults(it.toString())
        }

    }

}