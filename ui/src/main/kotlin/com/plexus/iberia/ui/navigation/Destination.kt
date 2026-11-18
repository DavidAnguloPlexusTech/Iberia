package com.plexus.iberia.ui.navigation

import kotlinx.serialization.Serializable

internal sealed interface Destination {

    @Serializable
    data object Splash : Destination

    @Serializable
    data object Home : Destination

    @Serializable
    data class Detail(val nasaId: String) : Destination
}
