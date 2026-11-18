package com.plexus.iberia.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.ScreenPlaceholder
import com.plexus.iberia.ui.theme.IberiaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SplashRoute(
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // Events are collected only while visible; Main.immediate handles them in the same frame.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                viewModel.events.collect { event ->
                    when (event) {
                        SplashEvent.NavigateToHome -> onNavigateToHome()
                    }
                }
            }
        }
    }
    SplashScreen(onAnimationFinished = viewModel::onAnimationFinished)
}

/** Temporary duration until the Lottie animation drives the end of the splash. */
private val SPLASH_DURATION = 1_500.milliseconds

@Composable
internal fun SplashScreen(onAnimationFinished: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnAnimationFinished by rememberUpdatedState(onAnimationFinished)
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION)
        currentOnAnimationFinished()
    }
    ScreenPlaceholder(title = stringResource(R.string.splash_title), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    IberiaTheme { SplashScreen(onAnimationFinished = {}) }
}
