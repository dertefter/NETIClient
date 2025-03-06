package com.dertefter.neticlient.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.FragmentBaseBinding

class BaseNavFragment : Fragment() {

    lateinit var binding: FragmentBaseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navGraphId = arguments?.getInt("NAV_GRAPH")
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(navGraphId!!)
        navController.navigate(navController.graph.startDestinationId)

    }

    fun getNavController(): NavController? {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host) as? NavHostFragment
        return navHostFragment?.navController
    }

}