package com.plexus.iberia.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.usecase.GetImageUseCase
import com.plexus.iberia.ui.common.asDomainError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class DetailState(
    val image: NasaImage? = null,
    val isLoading: Boolean = true,
    val error: DomainError? = null,
)

internal sealed interface DetailEvent {
    data object NavigateBack : DetailEvent
}

internal class DetailViewModel(
    private val nasaId: String,
    private val getImage: GetImageUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    init {
        loadImage()
    }

    fun retry() {
        loadImage()
    }

    fun onBackClick() {
        viewModelScope.launch { _events.emit(DetailEvent.NavigateBack) }
    }

    private fun loadImage() {
        _state.update { it.copy(isLoading = true, error = null) }
        getImage(nasaId)
            .onEach(::onImageResult)
            .launchIn(viewModelScope)
    }

    private fun onImageResult(result: Result<NasaImage>) {
        result.fold(
            onSuccess = { image -> _state.update { it.copy(image = image, isLoading = false) } },
            onFailure = { error -> _state.update { it.copy(isLoading = false, error = error.asDomainError()) } },
        )
    }
}
