package com.plexus.iberia.ui.splash.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.theme.IberiaTheme

private const val PULSES = 2
private const val PULSE_SCALE = 1.12f
private const val HALF_PULSE_MILLIS = 400

@Composable
internal fun PulsingLogo(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        repeat(PULSES) {
            scale.animateTo(PULSE_SCALE, halfPulse())
            scale.animateTo(1f, halfPulse())
        }
        currentOnFinished()
    }
    Image(
        painter = painterResource(R.drawable.nasa_logo),
        contentDescription = stringResource(R.string.nasa_logo),
        modifier = modifier
            .size(width = 165.dp, height = 138.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
    )
}

private fun halfPulse() = tween<Float>(durationMillis = HALF_PULSE_MILLIS, easing = FastOutSlowInEasing)

@Preview(showBackground = true)
@Composable
private fun PulsingLogoPreview() {
    IberiaTheme { PulsingLogo(onFinished = {}) }
}
