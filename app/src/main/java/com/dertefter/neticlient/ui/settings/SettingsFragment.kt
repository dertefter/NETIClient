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
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.appName.text = getString(R.string.app_name)
        val versionName = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        val versionCode = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionCode
        binding.appVerion.text = "$versionName ($versionCode)"

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


    fun showChangeDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_title, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
        val field = dialogView.findViewById<TextInputLayout>(R.id.field)
        val radioCustom = dialogView.findViewById<RadioButton>(R.id.radioCustom)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            field.visibility = if (checkedId == R.id.radioCustom) View.VISIBLE else View.GONE
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.edit))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                val selectedOption = when (radioGroup.checkedRadioButtonId) {
                    R.id.radioOption1 -> getString(R.string.app_name)
                    R.id.radioOption2 -> getString(R.string.title_nstu)
                    R.id.radioCustom -> field.editText!!.text.toString()
                    else -> ""
                }
                settingsViewModel.setDashboardTitle(selectedOption)

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    private fun setupSwitches() {

        binding.editTitle.setOnClickListener {
            showChangeDialog(requireContext())
        }

        binding.sliderNotify.value = settingsViewModel.notifyValue.value?.toFloat() ?: 15f

        binding.sliderNotify.addOnChangeListener { slider, value, fromUser ->
            settingsViewModel.setNotifyValue(value.toInt())
        }

        settingsViewModel.notifyValue.observe(viewLifecycleOwner){
            binding.sliderNotifyText.text = "За $it минут"
        }


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