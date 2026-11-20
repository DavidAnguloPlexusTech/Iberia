package com.plexus.iberia.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface SplashEvent {
    data object NavigateToHome : SplashEvent
}

internal class SplashViewModel : ViewModel() {

    private val _events = Channel<SplashEvent>(Channel.BUFFERED)
    val events: Flow<SplashEvent> = _events.receiveAsFlow()

    fun onAnimationFinished() {
        viewModelScope.launch { _events.send(SplashEvent.NavigateToHome) }
    }
}
