package com.dertefter.neticlient.ui.main

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.ThemeCompat
import androidx.core.graphics.Insets
import androidx.core.os.BuildCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.ActivityMainBinding
import com.dertefter.neticlient.services.ScheduleService
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.ui.messages.MessagesViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.max

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var navController: NavController
    private var keepSplashScreen = false

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeEngine.setup(this)
        val selectedTheme = ThemeEngine.getSelectedTheme()
        if (selectedTheme == 0) {
            setTheme(R.style.GreenTheme)
        } else {
            setTheme(selectedTheme)
        }

        if (VERSION.SDK_INT >= 31) {
            val splashScreen = this.splashScreen
            splashScreen.setSplashScreenTheme(selectedTheme)
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                keepSplashScreen
            }
        }


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsViewModel.scheduleServiceState.observe(this) {
            Log.e("settingsViewModel", it.toString())
            if (it == true) {
                Log.e("settingsViewModel.scheduleServiceState", "starting service")
                val intent = Intent(this, ScheduleService::class.java)
                startForegroundService(intent)
            } else {
                Log.e("settingsViewModel.scheduleServiceState", "stopping service")
                val intent = Intent(this, ScheduleService::class.java)
                stopService(intent)
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment

        navController = navHostFragment.navController

        mainActivityViewModel.startUpdatingTime()

        ViewGroupCompat.installCompatInsetsDispatch(binding.root)

        loginViewModel.authStateLiveData.observe(this){
            when (it) {
                AuthState.AUTHORIZED_WITH_ERROR -> {showAlert(getString(R.string.auth_error))}
                else -> binding.alertContainer.visibility = View.GONE
            }
        }

        binding.retryButton.setOnClickListener {
            loginViewModel.tryAuthorize()
        }

    }




    private var alertDismissRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    private fun showAlert(message: String) {
        binding.alertContainer.visibility = View.VISIBLE
        binding.alertTextView.text = message
        alertDismissRunnable?.let { handler.removeCallbacks(it) }
        alertDismissRunnable = Runnable {
            binding.alertContainer.visibility = View.GONE
        }
        handler.postDelayed(alertDismissRunnable!!, 6000)
    }

}