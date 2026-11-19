package com.plexus.iberia.ui.detail

import app.cash.turbine.test
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.repository.NasaImageRepository
import com.plexus.iberia.domain.usecase.GetImageUseCase
import com.plexus.iberia.ui.MainDispatcherRule
import com.plexus.iberia.ui.image
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: NasaImageRepository = mock()
    private var imageResult: Result<NasaImage> = Result.success(image("id"))
    private var responseDelay: Duration = Duration.ZERO
    private val viewModel by lazy { DetailViewModel(nasaId = "GRC-1", getImage = GetImageUseCase(repository)) }

    @Before
    fun setUp() {
        whenever(repository.getImage(any())).thenAnswer {
            flow {
                delay(responseDelay)
                emit(imageResult)
            }
        }
    }

    @Test
    fun `starts loading`() = runTest {
        responseDelay = 1.seconds
        viewModel
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `requests the image of its id`() = runTest {
        viewModel
        advanceUntilIdle()

        verify(repository).getImage("GRC-1")
    }

    @Test
    fun `shows the image once loaded`() = runTest {
        imageResult = Result.success(image("GRC-1"))
        viewModel
        advanceUntilIdle()

        assertEquals(image("GRC-1"), viewModel.state.value.image)
    }

    @Test
    fun `exposes the error when loading fails`() = runTest {
        imageResult = Result.failure(DomainError.NotFound())
        viewModel
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error is DomainError.NotFound)
    }

    @Test
    fun `retry loads the image again`() = runTest {
        imageResult = Result.failure(DomainError.Network())
        viewModel
        advanceUntilIdle()
        imageResult = Result.success(image("GRC-1"))
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
