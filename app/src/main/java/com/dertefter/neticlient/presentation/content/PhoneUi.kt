package com.dertefter.neticlient.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.dertefter.data.dto.auth.AuthStatus
import com.dertefter.design.theme.spacing
import com.dertefter.neticlient.navigation.AppNavHost
import com.dertefter.neticlient.navigation.TabRouteItem
import com.dertefter.neticlient.presentation.Event
import com.dertefter.neticlient.presentation.components.AuthNotifyCard
import com.dertefter.neticlient.presentation.components.MainBottomBar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PhoneUi(
    navController: NavHostController,
    currentDestination: NavDestination?,
    navigationItems: List<TabRouteItem> = emptyList(),
    onEvent: (Event) -> Unit,
    authStatusCiu: AuthStatus,
    authStatusYourNeti: AuthStatus,
    isFullScreen: Boolean
){


    val hazeState = rememberHazeState()

    var isAuthNotifyVisible by rememberSaveable(authStatusCiu.toString(), authStatusYourNeti.toString()) {
        mutableStateOf(authStatusCiu !is AuthStatus.Unauthorized)
    }

    LaunchedEffect(authStatusCiu, authStatusYourNeti) {
        if (authStatusCiu !is AuthStatus.Unauthorized && isAuthNotifyVisible) {
            delay(8000.milliseconds)
            isAuthNotifyVisible = false
        }
    }


    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !isFullScreen,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ){
                MainBottomBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    navigationItems = navigationItems,
                )
            }

        }
    ) { paddingValues ->
        val bottomPadding by animateDpAsState(
            if (isFullScreen) 0.dp else paddingValues.calculateBottomPadding()
        )

        Box(
            modifier = Modifier
                .consumeWindowInsets(WindowInsets(bottom = bottomPadding))
                .padding(bottom = bottomPadding)
                .imePadding()
                .fillMaxSize()
        ) {
            AppNavHost(
                modifier = Modifier
                    .hazeSource(state = hazeState),
                navController = navController,
                navigationItems
            )

            AnimatedVisibility (
                visible = isAuthNotifyVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                val dismissState = rememberSwipeToDismissBoxState()
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        isAuthNotifyVisible = false
                    }
                }
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {}
                ) {
                    AuthNotifyCard(
                        modifier = Modifier
                            .padding(MaterialTheme.spacing.medium),
                        ciuAuthStatus = authStatusCiu,
                        yourNetiAuthStatus = authStatusYourNeti,
                        onRetry = {
                            onEvent(Event.OnRetryAuthorizeCiu)
                        },
                        onRetryYourNeti = {
                            onEvent(Event.OnRetryAuthorizeYourNeti)
                        }
                    )
                }
            }
        }
    }


}
