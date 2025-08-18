package com.dertefter.neticlient.ui.search

import com.dertefter.neticlient.ui.search.NavSearchViewModel.NamedDestination

sealed class SearchItem {
    data class GroupItem(val name: String) : SearchItem()
    data class PersonItem(val id: String) : SearchItem()
    data class NavDestinationItem(val dist: NamedDestination) : SearchItem()
}