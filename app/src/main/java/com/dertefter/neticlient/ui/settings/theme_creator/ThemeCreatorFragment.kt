package com.dertefter.neticlient.ui.settings.theme_creator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.databinding.FragmentThemeCreatorBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ThemeCreatorFragment : Fragment() {
    private lateinit var binding: FragmentThemeCreatorBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private var updateColorJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemeCreatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.colorPicker.hueSliderView = binding.hueSlider
        binding.colorPicker.color = ThemeEngine.getSelectedColor()

        binding.createTheme.setOnClickListener {
            val color = binding.colorPicker.color
            ThemeEngine.setThemeType(1)
            ThemeEngine.setSelectedColor(color)
            requireActivity().recreate()
        }

        binding.buttonDefaultColor.setOnClickListener {
            ThemeEngine.setThemeType(0)
            ThemeEngine.setSelectedColor(null)
            requireActivity().recreate()
        }



    }

}

