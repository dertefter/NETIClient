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
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Подключаем свитчи к ViewModel
        setupSwitch("schedule_service") { settingsViewModel.setScheduleService(it) }
        setupSwitch("legendary_cards") { settingsViewModel.setLegendaryCards(it) }
        setupSwitch("notify_future_lessons") { settingsViewModel.setNotifyFutureLessons(it) }

        // Кнопки
        findPreference<Preference>("open_source_licenses")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
            true
        }

        findPreference<Preference>("github")?.setOnPreferenceClickListener {
            openUrl("https://github.com/dertefter/NETIClient")
            true
        }

        findPreference<Preference>("policy")?.setOnPreferenceClickListener {
            openUrl("https://docs.google.com/document/d/1D0f0Fp51h_Jj6MZro8nLKvSY2plH1CLZVD4js3ZFSYY/edit?usp=sharing")
            true
        }

        findPreference<Preference>("telegram")?.setOnPreferenceClickListener {
            openUrl("https://t.me/nstumobile_dev")
            true
        }
    }

    private fun setupSwitch(key: String, onChange: (Boolean) -> Unit) {
        val switchPref = findPreference<SwitchPreferenceCompat>(key)
        switchPref?.isChecked = when (key) {
            "schedule_service" -> settingsViewModel.scheduleServiceState.value ?: false
            "legendary_cards" -> settingsViewModel.legendaryCardsState.value ?: false
            "notify_future_lessons" -> settingsViewModel.notifyFutureLessonsState.value ?: false
            else -> false
        }

        switchPref?.setOnPreferenceChangeListener { _, newValue ->
            onChange(newValue as Boolean)
            true
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
