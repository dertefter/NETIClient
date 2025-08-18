package com.dertefter.neticlient.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph

class NavSearchViewModel() : ViewModel() {

   var navGraph: NavGraph? = null

    private val _searchResults = MutableLiveData<List<NamedDestination>>()
    val searchResults: LiveData<List<NamedDestination>> = _searchResults

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
        } else {
            if (navGraph == null) return
            val allLabeledDestinations: List<NamedDestination> = navGraph!!
                .getAllDestinations()
                .mapNotNull { destination ->
                    val label = destination.label?.toString()
                    if (!label.isNullOrBlank()) {
                        NamedDestination(label, destination.id)
                    } else {
                        null
                    }
                }

            _searchResults.value = allLabeledDestinations.filter {
                it.label.contains(trimmed, ignoreCase = true)
            }
        }
    }

    data class NamedDestination(
        val label: String,
        val id: Int
    )


    fun NavGraph.getAllDestinations(): List<NavDestination> {
        val result = mutableListOf<NavDestination>()
        this.forEach { destination ->
            result += destination
            if (destination is NavGraph) {
                result += destination.getAllDestinations()
            }
        }
        return result
    }
}
