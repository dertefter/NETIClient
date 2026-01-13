package com.dertefter.neticlient.ui.main

import android.app.ComponentCaller
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.ActivityMainBinding
import com.dertefter.neticlient.foreground.ScheduleService
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.main.theme_engine.ThemeEngine
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.widgets.NextLessonsWidgetProvider
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.view.size
import com.dertefter.shapemorphview.ShapeMorphView


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var picasso: Picasso
    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private lateinit var navController: NavController
    private var keepSplashScreen = true

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()


    fun setupThemeEngine(){
        ThemeEngine.setup(this)
        val themeType = ThemeEngine.getThemeType()
        when (themeType){
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
            else -> {
                DynamicColors.applyToActivityIfAvailable(this)
            }
        }
    }
    fun collectScheduleServiceState(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.scheduleServiceState.collect { state ->
                    val intent = Intent(this@MainActivity, ScheduleService::class.java)
                    if (state) {
                        startForegroundService(intent)
                    } else {
                        stopService(intent)
                    }
                }
            }
        }
    }
    fun setupNavigation(){
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavigation?.setupWithNavController(navController)
        binding.navigationRail?.setupWithNavController(navController)

        binding.profileButton.setOnClickListener {
            binding.bottomNavigation?.selectedItemId = R.id.nav_graph_profile
            binding.navigationRail?.selectedItemId = R.id.nav_graph_profile
        }
        binding.avatarShape.setOnClickListener{
            binding.bottomNavigation?.selectedItemId = R.id.nav_graph_profile
            binding.navigationRail?.selectedItemId = R.id.nav_graph_profile
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.verticalNav?.isGone = destination.id == R.id.newsDetailFragment || destination.id == R.id.onBoardingFragment
            binding.horizontalNav?.isGone = destination.id == R.id.newsDetailFragment || destination.id == R.id.onBoardingFragment
        }

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

    }

    fun collectOnBoarding(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.onBoardingState
                    .collect { state ->
                        showOnBoarding(state)
                    }
            }
        }
    }

    fun collectCurrentGroupForWidget(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.currentGroup.collect { currentGroup ->
                    if (currentGroup != null) {
                        val intent = Intent(this@MainActivity, NextLessonsWidgetProvider::class.java).apply {
                            action = "com.dertefter.neticlient.action.UPDATE_PROGRESS"
                        }
                        sendBroadcast(intent)
                    }
                }
            }
        }
    }


    fun adoptNavigationForAuthStatus(){
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStatus.collect { status ->
                    Log.e("MainActivity status", status.toString())
                    val menuSize = binding.bottomNavigation?.menu?.size ?: binding.navigationRail?.menu?.size!!
                    if (status == AuthStatusType.UNAUTHORIZED){
                        if ( menuSize != 2 ){
                            binding.bottomNavigation?.menu?.clear()
                            binding.bottomNavigation?.inflateMenu(R.menu.navigation_menu_guest)
                            binding.navigationRail?.menu?.clear()
                            binding.navigationRail?.inflateMenu(R.menu.navigation_menu_guest)
                        }
                    } else {
                        if ( menuSize != 5 ){
                            binding.bottomNavigation?.menu?.clear()
                            binding.bottomNavigation?.inflateMenu(R.menu.navigation_menu)
                            binding.navigationRail?.menu?.clear()
                            binding.navigationRail?.inflateMenu(R.menu.navigation_menu)
                        }
                    }

                }
            }
        }
    }

    fun collectAuthStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    loginViewModel.mobileAuthStatus,
                    loginViewModel.userDetailMobile
                ) { authStatus, userDetail ->
                    authStatus to userDetail
                }.collect { (authStatus, userDetail) ->

                    binding.statusGuest.isGone = authStatus != AuthStatusType.UNAUTHORIZED
                    binding.statusLoading.isGone = authStatus != AuthStatusType.LOADING
                    binding.statusSuccess.isGone = authStatus != AuthStatusType.AUTHORIZED
                    binding.statusError.isGone = authStatus != AuthStatusType.AUTHORIZED_WITH_ERROR

                    if (authStatus != AuthStatusType.LOADING){
                        keepSplashScreen = false
                    }

                    if (!userDetail?.photoPath.isNullOrEmpty()){
                        picasso
                            .load(userDetail.photoPath)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    bitmap?.let{
                                        (binding.avatarShape as ShapeMorphView).setBitmap(bitmap)
                                    }
                                }
                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                            })


                    }

                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }


        Handler(Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 3000)


        setupThemeEngine()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewGroupCompat.installCompatInsetsDispatch(binding.root)

        collectScheduleServiceState()

        setupNavigation()

        mainActivityViewModel.startUpdatingTime()

        handleShortcutIntent(intent)

        collectOnBoarding()

        collectCurrentGroupForWidget()

        collectAuthStatus()

        adoptNavigationForAuthStatus()

    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent) {
        val destination = intent.getStringExtra("shortcut_id")
        when (destination) {

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