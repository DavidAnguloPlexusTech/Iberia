package com.plexus.iberia.ui.navigation

import androidx.navigation.NavHostController

/** Opens Home and removes the splash from the back stack. */
internal fun NavHostController.navigateToHome() {
    navigate(Routes.Home) {
        popUpTo(Routes.Splash) { inclusive = true }
    }
}

internal fun NavHostController.navigateToDetail(nasaId: String) {
    navigate(Routes.Detail(nasaId))
}
