package com.plexus.iberia.ui.detail

import app.cash.turbine.test
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.usecase.GetImageUseCase
import com.plexus.iberia.ui.FakeNasaImageRepository
import com.plexus.iberia.ui.MainDispatcherRule
import com.plexus.iberia.ui.image
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNasaImageRepository()
    private val viewModel by lazy { DetailViewModel(nasaId = "GRC-1", getImage = GetImageUseCase(repository)) }

    @Test
    fun `starts loading`() = runTest {
        repository.responseDelay = 1.seconds
        viewModel
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `shows the image once loaded`() = runTest {
        repository.imageResult = Result.success(image("GRC-1"))
        viewModel
        advanceUntilIdle()

        assertEquals(image("GRC-1"), viewModel.state.value.image)
    }

    @Test
    fun `exposes the error when loading fails`() = runTest {
        repository.imageResult = Result.failure(DomainError.NotFound())
        viewModel
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error is DomainError.NotFound)
    }

    @Test
    fun `retry loads the image again`() = runTest {
        repository.imageResult = Result.failure(DomainError.Network())
        viewModel
        advanceUntilIdle()
        repository.imageResult = Result.success(image("GRC-1"))
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(image("GRC-1"), viewModel.state.value.image)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `navigates back when back is clicked`() = runTest {
        viewModel.events.test {
            viewModel.onBackClick()

            assertEquals(DetailEvent.NavigateBack, awaitItem())
        }
    }
}
