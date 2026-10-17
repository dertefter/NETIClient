package com.dertefter.neticlient.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dertefter.data.dto.auth.AuthStatus
import com.dertefter.data.dto.settings.ThemeStyle
import com.dertefter.design.theme.AppTheme
import com.dertefter.design.theme.cornerShape
import com.dertefter.design.theme.isTab
import com.dertefter.design.theme.rounding
import com.dertefter.doc_detail.DocDetailRoute
import com.dertefter.lesson_detail.LessonDetailRoute
import com.dertefter.navigation.NavigationAction
import com.dertefter.navigation.Navigator
import com.dertefter.navigation.Routes
import com.dertefter.neticlient.navigation.getNavigationMenu
import com.dertefter.neticlient.presentation.content.PhoneUi
import com.dertefter.neticlient.presentation.content.TabUI
import com.dertefter.search_group.SearchGroupRoute
import com.dertefter.share_score.ShareScoreRoute
import com.dertefter.swap_lks.SwapLksRoute
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigator: Navigator,
    screenState: MainScreenState,
    onEvent: (Event) -> Unit,
    themeState: ThemeState,
) {

    AppTheme(
        seedColor = themeState.seedColor,
        isCut = themeState.isShapeCut,
        specVersion = if (themeState.oldColorSpecVersion) ColorSpec.SpecVersion.SPEC_2021 else ColorSpec.SpecVersion.SPEC_2025,
        paletteStyle = when (themeState.themeStyle) {
            ThemeStyle.Neutral -> PaletteStyle.Neutral
            ThemeStyle.TonalSpot -> PaletteStyle.TonalSpot
            ThemeStyle.Vibrant -> PaletteStyle.Vibrant
            ThemeStyle.Expressive -> PaletteStyle.Expressive
        }
    ) {

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        var bottomSheetRoute by remember { mutableStateOf<Routes?>(null) }
        val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)


        val isFullScreen = when {
            currentDestination?.hasRoute<Routes.NewsDetail>() == true -> true
            currentDestination?.hasRoute<Routes.ImageViewer>() == true -> true
            else -> false
        }


        val navigationItems =
            getNavigationMenu(screenState.authStatusCiu !is AuthStatus.Unauthorized)

        LaunchedEffect(Unit) {
            navigator.navigationActions.collect { action ->
                when (action) {
                    is NavigationAction.Navigate -> {
                        sheetState.hide()
                        navController.navigate(action.route)
                    }

                    is NavigationAction.NavigateAndClearBackStack -> {
                        navController.navigate(action.route) {
                            popUpTo(action.popupTo) {
                                inclusive = action.inclusive
                            }
                        }
                    }

                    NavigationAction.NavigateUp -> {
                        navController.navigateUp()
                    }

                    is NavigationAction.OpenAsBottomSheet -> {
                        bottomSheetRoute = action.route
                    }

                    is NavigationAction.HideBottomSheet -> {
                        sheetState.hide()
                        bottomSheetRoute = null
                    }

                }
            }
        }

        if (MaterialTheme.isTab) {
            TabUI(
                navController = navController,
                currentDestination = currentDestination,
                navigationItems = navigationItems,
                authStatusCiu = screenState.authStatusCiu,
                authStatusYourNeti = screenState.authStatusYourNeti,
                onEvent = onEvent,
                isFullScreen = isFullScreen
            )
        } else {
            PhoneUi(
                navController = navController,
                currentDestination = currentDestination,
                navigationItems = navigationItems,
                authStatusCiu = screenState.authStatusCiu,
                authStatusYourNeti = screenState.authStatusYourNeti,
                onEvent = onEvent,
                isFullScreen = isFullScreen
            )
        }


        AppBottomSheet(
            bottomSheetRoute = bottomSheetRoute,
            onDismissRequest = { bottomSheetRoute = null },
            sheetState = sheetState
        )


    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBottomSheet(
    bottomSheetRoute: Routes?,
    onDismissRequest: () -> Unit,
    sheetState: SheetState
) {
    if (bottomSheetRoute != null) {
        val bottomSheetCornerRadius by animateDpAsState(
            targetValue = if (sheetState.targetValue == SheetValue.Expanded) 0.dp else MaterialTheme.rounding.extraLargeIncreased,
            label = "bottomSheetCornerRadius",
            animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
        )

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = MaterialTheme.cornerShape(
                topStart = bottomSheetCornerRadius.coerceAtLeast(0.dp),
                topEnd = bottomSheetCornerRadius.coerceAtLeast(0.dp)
            )
        ) {
            when (bottomSheetRoute) {
                is Routes.SearchGroup -> SearchGroupRoute()
                is Routes.ShareScore -> ShareScoreRoute()
                is Routes.SwapLks -> SwapLksRoute()
                is Routes.LessonDetail -> LessonDetailRoute(
                    name = bottomSheetRoute.name,
                    type = bottomSheetRoute.type,
                    aud = bottomSheetRoute.aud,
                    personIds = bottomSheetRoute.personIds,
                    startTime = bottomSheetRoute.getStartTime(),
                    endTime = bottomSheetRoute.getEndTime(),
                    date = bottomSheetRoute.getDate()
                )

                is Routes.DocDetail -> DocDetailRoute(
                    type = bottomSheetRoute.type,
                    date = bottomSheetRoute.date,
                    status = bottomSheetRoute.status,
                    person = bottomSheetRoute.person,
                    comment = bottomSheetRoute.comment,
                    number = bottomSheetRoute.number
                )

                else -> {}
            }
        }
    }
}
