package com.plexus.iberia.ui.splash

import app.cash.turbine.test
import com.plexus.iberia.ui.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `navigates to home when the animation finishes`() = runTest {
        val viewModel = SplashViewModel()

        viewModel.events.test {
            viewModel.onAnimationFinished()

            assertEquals(SplashEvent.NavigateToHome, awaitItem())
        }
    }
}
