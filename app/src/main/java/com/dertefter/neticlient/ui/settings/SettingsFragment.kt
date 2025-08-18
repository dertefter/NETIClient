package com.dertefter.neticlient.ui.settings

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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                binding.notificationPermissionCard.isVisible = false
            }
        }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.updateNotificationPermissionState(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.appName.text = getString(R.string.app_name)
        val versionName = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        val versionCode = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionCode
        binding.appVerion.text = "$versionName ($versionCode)"


        binding.editColor.setOnClickListener {
            findNavController().navigate(
                R.id.themeCreatorFragment,
                null,
                Utils.getNavOptions(),
            )
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }


        binding.githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                "https://github.com/dertefter/NETIClient".toUri())
            startActivity(intent)
        }

        binding.policyButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                "https://docs.google.com/document/d/1D0f0Fp51h_Jj6MZro8nLKvSY2plH1CLZVD4js3ZFSYY/edit?usp=sharing".toUri())
            startActivity(intent)
        }

        binding.tgButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://t.me/nstumobile_dev".toUri())
            startActivity(intent)
        }

        binding.permissionButton.setOnClickListener {
            requestPermission()
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                settingsViewModel.isGrantedPermission.collect { state ->
                    binding.notificationPermissionCard.isGone = state
                    binding.switchNotifyLessons.isEnabled = state
                    binding.switchNotifyFutureLessons.isEnabled = state
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.scheduleServiceState.collect { state ->
                    binding.notifyFutureLessonsCard.isGone = !state
                    binding.switchNotifyLessons.isChecked = state
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.notifyFutureLessonsState.collect { state ->
                    binding.notifyFutureLessonsValueCard.isGone = !state
                    binding.switchNotifyFutureLessons.isChecked = state
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.notifyFutureLessonsValueState.collect { state ->
                    if (state % 15 != 0){
                        settingsViewModel.setNotifyValue(15)
                    } else {
                        binding.sliderNotify.value = state.toFloat()
                        binding.sliderNotifyText.text = "За $state минут"
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.materialYouState.collect { state ->
                    binding.switchDynamicColor.setOnCheckedChangeListener(null)

                    binding.editColor.isGone = state
                    binding.switchDynamicColor.isChecked = state

                    binding.switchDynamicColor.setOnCheckedChangeListener { _, v ->
                        settingsViewModel.setMaterialYou(v)

                        val selectedThemeType = ThemeEngine.getThemeType()

                        val newThemeType = if (v){
                            2
                        } else {
                            0
                        }
                        ThemeEngine.setThemeType(newThemeType)

                        if (selectedThemeType != newThemeType){
                            requireActivity().recreate()
                        }

                    }


                }
            }
        }

        if (!DynamicColors.isDynamicColorAvailable()){
            settingsViewModel.setMaterialYou(false)
            binding.switchDynamicColor.isEnabled = false
        }else{
            binding.switchDynamicColor.isEnabled = true
        }


        binding.switchNotifyLessons.setOnCheckedChangeListener { _, v ->
            settingsViewModel.setScheduleService(v)
        }

        binding.switchNotifyFutureLessons.setOnCheckedChangeListener { _, v ->
            settingsViewModel.setNotifyFutureLessons(v)
        }



        binding.sliderNotify.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                settingsViewModel.setNotifyValue(value.toInt())
            }
        }



    }





}

