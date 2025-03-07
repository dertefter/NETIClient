package com.dertefter.neticlient.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentMessagesBinding
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.utils.GridSpacingItemDecoration
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    // Ланчер для запроса разрешения
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handlePermissionResult(isGranted)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
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

        checkNotificationPermission()
        setupPermissionUI()
        setupSwitches()
    }

    private fun checkNotificationPermission() {
        val hasPermission = isNotificationPermissionGranted()
        updateUIBasedOnPermission(hasPermission)
    }

    private fun updateUIBasedOnPermission(hasPermission: Boolean) {
        binding.notificationCard.visibility = if (hasPermission) View.GONE else View.VISIBLE
        binding.switchScheduleService.isEnabled = hasPermission
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            updateUIBasedOnPermission(true)
            binding.switchScheduleService.isChecked = true
            settingsViewModel.setScheduleService(true)
        } else {
            // Дополнительная логика при отказе
        }
    }

    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupPermissionUI() {
        binding.permissionButton.setOnClickListener {
            if (!isNotificationPermissionGranted()) {
                requestPermission()
            }
        }
    }

    private fun setupSwitches() {
        binding.switchScheduleService.isChecked = settingsViewModel.scheduleServiceState.value == true
        binding.switchScheduleService.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setScheduleService(isChecked)
        }
        settingsViewModel.scheduleServiceState.observe(viewLifecycleOwner){
            (binding.switchNotifyLessons.parent as View).visibility = if (it == true) View.VISIBLE else View.GONE
        }

        binding.switchNotifyLessons.isChecked = settingsViewModel.notifyFutureLessonsState.value == true
        binding.switchNotifyLessons.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setNotifyFutureLessons(isChecked)
        }


    }

}