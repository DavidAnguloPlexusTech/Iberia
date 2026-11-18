package com.plexus.iberia.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.plexus.iberia.ui.splash.components.PulsingLogo
import com.plexus.iberia.ui.theme.IberiaTheme
import kotlinx.coroutines.Dispatchers
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

/** Stateless: shows the pulsing NASA logo and reports when the animation ends. */
@Composable
internal fun SplashScreen(onAnimationFinished: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PulsingLogo(onFinished = onAnimationFinished)
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    IberiaTheme { SplashScreen(onAnimationFinished = {}) }
}
