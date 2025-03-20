package com.dertefter.neticlient.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()


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

        binding.themesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)



        val themes = mutableListOf<Int>(
            R.style.RedTheme,
            R.style.OrangeTheme,
            R.style.YellowTheme,
            R.style.GreenTheme,
            R.style.TealTheme,
            R.style.LightBlueTheme,
            R.style.RoyalTheme,
            R.style.PinkTheme,
            R.style.LegendaryTheme,
            R.style.DynamicTheme
        )

        binding.themesRecyclerView.addItemDecoration(GridSpacingItemDecoration(requireContext(),themes.size, R.dimen.margin_min))

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adapter = ThemeAdapter() { theme ->
            ThemeEngine.setSelectedTheme(theme)
            requireActivity().recreate()
        }
        val selectedTheme = ThemeEngine.getSelectedTheme()

        adapter.setThemesList(themes, selectedTheme)


        binding.themesRecyclerView.adapter = adapter

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

        binding.switchLegendaeyCard.isChecked = settingsViewModel.legendaryCardsState.value == true
        binding.switchLegendaeyCard.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setLegendaryCards(isChecked)
        }


        settingsViewModel.scheduleServiceState.observe(viewLifecycleOwner){
            (binding.switchNotifyLessons.parent as View).visibility = if (it == true) View.VISIBLE else View.GONE
        }

        binding.switchNotifyLessons.isChecked = settingsViewModel.notifyFutureLessonsState.value == true
        binding.switchNotifyLessons.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setNotifyFutureLessons(isChecked)
        }

        binding.switchNotifyLessons.isChecked = settingsViewModel.notifyFutureLessonsState.value == true
        binding.switchNotifyLessons.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setNotifyFutureLessons(isChecked)
        }

    }

}