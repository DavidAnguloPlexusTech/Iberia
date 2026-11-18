package com.plexus.iberia.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.ScreenPlaceholder
import com.plexus.iberia.ui.theme.IberiaTheme
import kotlinx.coroutines.delay

/** Temporary duration until the Lottie animation drives the end of the splash. */
private const val SPLASH_DURATION_MILLIS = 1_500L

@Composable
internal fun SplashScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MILLIS)
        currentOnFinished()
    }
    ScreenPlaceholder(title = stringResource(R.string.splash_title), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    IberiaTheme { SplashScreen(onFinished = {}) }
}
