package com.dertefter.neticlient.ui.main

import android.app.ComponentCaller
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.ActivityMainBinding
import com.dertefter.neticlient.services.ScheduleService
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.ui.on_boarding.OnBoardingFragment
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.Dispatcher
import java.io.File


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()


    private lateinit var navController: NavController
    private var keepSplashScreen = true

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }


        Handler(Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 200)

        ThemeEngine.setup(this)

        val themeType = ThemeEngine.getThemeType()


        when (themeType){
            0 -> {}
            1 -> {
                val selectedColor = ThemeEngine.getSelectedColor()
                if (selectedColor != 0){
                    DynamicColors.applyToActivityIfAvailable(
                        this,
                        DynamicColorsOptions.Builder()
                            .setContentBasedSource(selectedColor)
                            .build()
                    )
                }
            }
            2 -> {
                DynamicColors.applyToActivityIfAvailable(this)
            }
        }

        scheduleViewModel.observeScheduleAndUpdateWidget(this)



        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.scheduleServiceState.collect { state ->
                    if (state) {
                        val intent = Intent(this@MainActivity, ScheduleService::class.java)
                        startForegroundService(intent)
                    } else {
                        val intent = Intent(this@MainActivity, ScheduleService::class.java)
                        stopService(intent)
                    }
                }
            }
        }




        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavigation?.setupWithNavController(navController)
        binding.navigationRail?.setupWithNavController(navController)
        binding.profileButton.setOnClickListener {
            navController.navigate(R.id.nav_graph_profile)
        }
        binding.avatarShape.setOnClickListener{
            navController.navigate(R.id.nav_graph_profile)
        }

        val showNavIds = listOf(
            R.id.messagesFragment,
            R.id.homeFragment,
            R.id.dashboardFragment
        )

        navController.addOnDestinationChangedListener {
                controller, destination, arguments ->
            binding.verticalNav?.isVisible = showNavIds.contains(destination.id)
            binding.horizontalNav?.isVisible = showNavIds.contains(destination.id)
        }






        mainActivityViewModel.startUpdatingTime()

        ViewGroupCompat.installCompatInsetsDispatch(binding.root)

        if (binding.horizontalNav != null){
            ViewCompat.setOnApplyWindowInsetsListener(binding.horizontalNav!!) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    left = bars.left,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                WindowInsetsCompat.CONSUMED
            }
        }

        if (binding.verticalNav != null){
            ViewCompat.setOnApplyWindowInsetsListener(binding.verticalNav!!) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    left = bars.left,
                    bottom = bars.bottom,
                    top = bars.top
                )
                WindowInsetsCompat.CONSUMED
            }
        }


        keepSplashScreen = false

        handleShortcutIntent(intent)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.onBoardingState
                    .collect { state ->
                        showOnBoarding(state)
                    }
            }
        }


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStatus.collect { authStatus ->

                    binding.authImageview.isGone =
                        authStatus != AuthStatusType.UNAUTHORIZED && authStatus != AuthStatusType.AUTHORIZED_WITH_ERROR
                    binding.authLoading.isGone = authStatus != AuthStatusType.LOADING
                    binding.authAvatarShapeContainer.isGone = authStatus != AuthStatusType.AUTHORIZED

                    if (authStatus != AuthStatusType.UNAUTHORIZED){
                        binding.bottomNavigation?.menu?.clear()
                        binding.navigationRail?.menu?.clear()
                        binding.bottomNavigation?.inflateMenu(R.menu.navigation_menu)
                        binding.navigationRail?.inflateMenu(R.menu.navigation_menu)
                    } else {
                        binding.bottomNavigation?.menu?.clear()
                        binding.navigationRail?.menu?.clear()
                        binding.bottomNavigation?.inflateMenu(R.menu.navigation_menu_guest)
                        binding.navigationRail?.inflateMenu(R.menu.navigation_menu_guest)
                    }

                    binding.bottomNavigation?.setupWithNavController(navController)
                    binding.navigationRail?.setupWithNavController(navController)


                }
            }
        }


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.userDetailMobile.collect { userDetail ->
                    Log.e("userMoble", userDetail.toString())
                    if (!userDetail?.photoPath.isNullOrEmpty()){

                        Picasso.get()
                            .load(userDetail.photoPath)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    binding.avatarShape.setImageBitmap(bitmap)
                                }

                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                    // обработка ошибки
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    // можно показать плейсхолдер
                                }
                            })


                    }
                }
            }
        }


    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent) {
        val destination = intent.getStringExtra("shortcut_id")
        when (destination) {
            "profile" -> {
                navController.navigate(
                    R.id.profileFragment,
                    null,
                    Utils.getNavOptions(),
                )
            }
            "schedule" -> {
                navController.navigate(
                    R.id.scheduleFragment,
                    null,
                    Utils.getNavOptions(),
                )
            }
        }
    }

    fun showOnBoarding(v: Boolean) {
        if (v) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .build()

            navController.navigate(R.id.onBoardingFragment, null, navOptions)
        }
    }

    fun cancelOnBoarding(){
        if (navController.currentDestination?.id == R.id.onBoardingFragment){
            navController.navigate(R.id.nav_graph_home)
        }
    }

}