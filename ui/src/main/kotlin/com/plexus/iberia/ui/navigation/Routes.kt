package com.plexus.iberia.ui.navigation

import kotlinx.serialization.Serializable

internal sealed interface Routes {

    @Serializable
    data object Splash : Routes

    @Serializable
    data object Home : Routes

    @Serializable
    data class Detail(val nasaId: String) : Routes
}
