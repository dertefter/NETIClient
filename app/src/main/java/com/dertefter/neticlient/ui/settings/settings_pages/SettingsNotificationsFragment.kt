package com.dertefter.neticlient.ui.settings.settings_pages

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.databinding.FragmentSettingsNotificationsBinding
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.dertefter.neticlient.ui.messages.MessagesAdapter
import com.dertefter.neticlient.ui.settings.SettingsListAdapter
import com.dertefter.neticlient.ui.settings.SettingsListItem
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SettingsNotificationsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsNotificationsBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            settingsViewModel.updateNotificationPermissionState(requireContext())
        }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge(binding.appBarLayout))

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            binding.scrollView.updatePadding(
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }


        settingsViewModel.updateNotificationPermissionState(requireContext())

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                settingsViewModel.scheduleServiceState.collect { scheduleState ->
                    binding.notifyNowSwitch.isChecked = scheduleState
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                settingsViewModel.isGrantedPermission.collect { isGrantedPermission ->
                    binding.notifyNowSwitch.isEnabled = isGrantedPermission
                    binding.permissionCard.isGone = isGrantedPermission
                }
            }
        }

        binding.permissionCard.setOnClickListener {
            requestPermission()
        }



        binding.notifyNowSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setScheduleService(isChecked)
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


    }





}

