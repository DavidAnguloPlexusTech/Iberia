package com.plexus.iberia.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal sealed interface SplashEvent {
    data object NavigateToHome : SplashEvent
}

internal class SplashViewModel : ViewModel() {

    private val _events = MutableSharedFlow<SplashEvent>()
    val events: SharedFlow<SplashEvent> = _events.asSharedFlow()

    fun onAnimationFinished() {
        viewModelScope.launch { _events.emit(SplashEvent.NavigateToHome) }
    }
}
