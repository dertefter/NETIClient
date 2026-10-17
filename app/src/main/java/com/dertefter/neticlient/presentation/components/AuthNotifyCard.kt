package com.dertefter.neticlient.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dertefter.data.dto.auth.AuthStatus
import com.dertefter.design.components.loading.AppLoadingIndicator
import com.dertefter.design.theme.AppTheme
import com.dertefter.design.theme.cornerShape
import com.dertefter.design.theme.rounding
import com.dertefter.design.theme.spacing
import com.dertefter.neticlient.R


@Composable
fun AuthNotifyCard(
    modifier: Modifier = Modifier,
    ciuAuthStatus: AuthStatus,
    yourNetiAuthStatus: AuthStatus,
    onRetry: () -> Unit = {},
    onRetryYourNeti: () -> Unit = {}
) {

    val motionScheme = MaterialTheme.motionScheme
    var isExpanded by remember { mutableStateOf(false) }

    val radius by animateDpAsState(
        targetValue = if (!isExpanded){
            MaterialTheme.rounding.extraExtraLarge
        } else {
            MaterialTheme.rounding.largeIncreased
        },
        animationSpec = motionScheme.fastSpatialSpec()
    )

    val contentColor by animateColorAsState(
        targetValue = when (ciuAuthStatus) {
            is AuthStatus.Loading -> MaterialTheme.colorScheme.onSecondary
            is AuthStatus.Authorized -> MaterialTheme.colorScheme.onPrimary
            is AuthStatus.Error -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "containerColor",
        animationSpec = motionScheme.defaultEffectsSpec()
    )

    val containerColor by animateColorAsState(
        targetValue = when (ciuAuthStatus) {
            is AuthStatus.Loading -> MaterialTheme.colorScheme.secondary
            is AuthStatus.Authorized -> MaterialTheme.colorScheme.primary
            is AuthStatus.Error -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "contentColor",
        animationSpec = motionScheme.defaultEffectsSpec()
    )

    Column(
        modifier = modifier
            .clip(MaterialTheme.cornerShape(radius))
            .clickable { isExpanded = !isExpanded }
            .background(containerColor)
            .animateContentSize(
                animationSpec = motionScheme.fastSpatialSpec()
            )
            .padding(MaterialTheme.spacing.medium)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedContent(
                targetState = ciuAuthStatus,
                transitionSpec = {
                    (slideInVertically(animationSpec = motionScheme.fastSpatialSpec()) { height -> height } + fadeIn(
                        animationSpec = motionScheme.defaultEffectsSpec()
                    )) togetherWith
                            (slideOutVertically(animationSpec = motionScheme.fastSpatialSpec()) { height -> -height } + fadeOut(
                                animationSpec = motionScheme.defaultEffectsSpec()
                            ))
                },
                label = "authStatusText",
                modifier = Modifier.weight(1f)
            )
            { targetAuthStatus ->
                val title = when (targetAuthStatus) {
                    is AuthStatus.Loading -> stringResource(R.string.app_loading)
                    is AuthStatus.Authorized -> stringResource(R.string.app_success)
                    is AuthStatus.Error -> stringResource(R.string.app_error)
                    else -> null
                }

                val caption = when (targetAuthStatus) {
                    is AuthStatus.Loading -> targetAuthStatus.login
                    is AuthStatus.Authorized -> targetAuthStatus.login
                    is AuthStatus.Error -> stringResource(R.string.app_learn_more)
                    else -> null
                }

                Column(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large)
                ) {
                    title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = contentColor
                        )
                    }

                    AnimatedVisibility(
                        visible = !isExpanded,
                        enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
                        exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                    ) {
                        caption?.let { caption ->
                            Text(
                                text = caption,
                                style = MaterialTheme.typography.labelMediumEmphasized,
                                color = contentColor
                            )
                        }
                    }


                }
            }

            AnimatedVisibility(
                visible = ciuAuthStatus is AuthStatus.Loading,
                enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
                exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
            ) {
                AppLoadingIndicator(
                    color = contentColor,
                )
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
            exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
        ) {
            Column(
                modifier = Modifier
                    .padding(top = MaterialTheme.spacing.medium)
                    .clip(MaterialTheme.shapes.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                SourceCard(
                    title = "CIU",
                    authStatus = ciuAuthStatus,
                    onRetry = onRetry
                )
                AnimatedVisibility(
                    visible = ciuAuthStatus is AuthStatus.Authorized,
                    enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
                    exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                ) {
                    SourceCard(
                        title = "YourNeti",
                        authStatus = yourNetiAuthStatus,
                        onRetry = onRetryYourNeti
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun AuthNotifyCardLoadingPreview() {
    AppTheme {
        AuthNotifyCard(
            modifier = Modifier.padding(24.dp),
            AuthStatus.Loading("ivanov_ii"),
            AuthStatus.Loading("ivanov_ii"),
        )
    }
}

@Preview(showBackground = true, name = "Authorized")
@Composable
fun AuthNotifyCardAuthorizedPreview() {
    AppTheme {
        AuthNotifyCard(
            modifier = Modifier.padding(24.dp),
            AuthStatus.Authorized("ivanov_ii"),
            AuthStatus.Authorized("ivanov_ii"),
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun AuthNotifyCardErrorPreview() {
    AppTheme {
        AuthNotifyCard(
            modifier = Modifier.padding(24.dp),
            AuthStatus.Error("ivanov_ii"),
            AuthStatus.Error("ivanov_ii"),
        )
    }
}
