package com.dertefter.neticlient.ui.settings.settings_pages

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentThemeCreatorBinding
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.ui.settings.theme_creator.ColorAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsPersonaliztionFragment : Fragment() {
    private lateinit var binding: FragmentThemeCreatorBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private val presetColors = listOf(
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#673AB7"), // Deep Purple
        Color.parseColor("#3F51B5"), // Indigo
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#03A9F4"), // Light Blue
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#009688"), // Teal
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#8BC34A"), // Light Green
        Color.parseColor("#CDDC39"), // Lime
        Color.parseColor("#FFEB3B"), // Yellow
        Color.parseColor("#FFC107"), // Amber
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#FF5722"), // Deep Orange
        Color.parseColor("#795548"), // Brown
        Color.parseColor("#9E9E9E"), // Grey
        Color.parseColor("#607D8B"), // Blue Grey
        Color.parseColor("#000000")  // Black
    )

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

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))


        val currentColor = ThemeEngine.getSelectedColor()

        val adapter = ColorAdapter(presetColors, currentColor) { selectedColor ->
            ThemeEngine.setThemeType(1)
            ThemeEngine.setSelectedColor(selectedColor)
            requireActivity().recreate()
        }

        binding.colorsRecyclerView.adapter = adapter

        binding.buttonDefaultColor.setOnClickListener {
            ThemeEngine.setThemeType(0)
            ThemeEngine.setSelectedColor(null)
            requireActivity().recreate()
        }
    }
}