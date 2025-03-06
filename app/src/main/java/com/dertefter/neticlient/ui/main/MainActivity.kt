package com.dertefter.neticlient.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import kotlin.math.max

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var pagerAdapter: MainActivityPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsViewModel.scheduleServiceState.observe(this){
            Log.e("settingsViewModel", it.toString())
            if (it == true){
                Log.e("settingsViewModel.scheduleServiceState", "starting service")
                val intent = Intent(this, ScheduleService::class.java)
                startForegroundService(intent)
            } else {
                Log.e("settingsViewModel.scheduleServiceState", "stopping service")
                val intent = Intent(this, ScheduleService::class.java)
                stopService(intent)
            }
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentByTag("f${binding.viewpager.currentItem}") as? BaseNavFragment
                val navController = currentFragment?.getNavController()

                if (navController?.currentDestination?.id != navController?.graph?.startDestinationId) {
                    navController?.popBackStack()
                } else {
                    finish()
                }
            }
        })


        pagerAdapter = MainActivityPagerAdapter(this)
        pagerAdapter.setData(
            intArrayOf(
                R.navigation.nav_graph_schedule,
                R.navigation.nav_graph_news,
                R.navigation.nav_graph_messages,
                R.navigation.nav_graph_profile
            ).toList()
        )
        mainActivityViewModel.startUpdatingTime()
        binding.viewpager.adapter = pagerAdapter
        binding.viewpager.isUserInputEnabled = false
        binding.viewpager.setOffscreenPageLimit(5)
        binding.bottomNavigation?.setOnItemSelectedListener { item ->
            var pos = 0
            when (item.itemId) {
                R.id.nav_graph_schedule -> pos = 0
                R.id.nav_graph_news -> pos = 1
                R.id.nav_graph_messages -> pos = 2
                R.id.nav_graph_profile -> pos = 3
            }
            binding.viewpager.setCurrentItem(pos, false)
            Utils.basicAnimationOn(binding.viewpager).start()

            true
        }

        binding.navigationRail?.setOnItemSelectedListener { item ->
            var pos = 0
            when (item.itemId) {
                R.id.nav_graph_schedule -> pos = 0
                R.id.nav_graph_news -> pos = 1
                R.id.nav_graph_messages -> pos = 2
                R.id.nav_graph_profile -> pos = 3
            }
            binding.viewpager.setCurrentItem(pos, false)
            Utils.basicAnimationOn(binding.viewpager).start()

            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val deviceInsets: Insets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            settingsViewModel.insetsViewModel.postValue(intArrayOf(deviceInsets.top, deviceInsets.bottom, deviceInsets.right, deviceInsets.left))
            WindowInsetsCompat.CONSUMED
        }


        settingsViewModel.insetsViewModel.observe(this){
            binding.bottomNavigation?.updatePadding(
                bottom = it[1],
                right = it[2],
                left = it[3]
            )
            binding.alertContainer.updatePadding(
                top = it[0]
            )
        }

        loginViewModel.authStateLiveData.observe(this){
            when (it) {
                AuthState.AUTHORIZED_WITH_ERROR -> showAlert("Ошибка авторизации")
                else -> binding.alertContainer.visibility = View.GONE
            }
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
        handler.postDelayed(alertDismissRunnable!!, 3000)
    }

}