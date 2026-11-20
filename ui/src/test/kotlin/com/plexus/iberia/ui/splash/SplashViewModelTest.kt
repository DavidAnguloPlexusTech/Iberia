package com.plexus.iberia.ui.splash

import app.cash.turbine.test
import com.plexus.iberia.ui.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.Test

class SplashViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    @Test
    fun `navigates to home when the animation finishes`() = runTest {
        val viewModel = SplashViewModel()

        viewModel.events.test {
            viewModel.onAnimationFinished()

            assertEquals(SplashEvent.NavigateToHome, awaitItem())
        }
    }
}
