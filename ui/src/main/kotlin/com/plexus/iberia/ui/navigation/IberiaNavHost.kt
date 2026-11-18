package com.plexus.iberia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.plexus.iberia.ui.detail.DetailRoute
import com.plexus.iberia.ui.home.HomeRoute
import com.plexus.iberia.ui.splash.SplashRoute

@Composable
internal fun IberiaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier,
    ) {
        splashDestination(onNavigateToHome = navController::navigateToHome)
        homeDestination(onNavigateToDetail = navController::navigateToDetail)
        detailDestination(onNavigateBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.splashDestination(onNavigateToHome: () -> Unit) {
    composable<Routes.Splash> { SplashRoute(onNavigateToHome = onNavigateToHome) }
}

private fun NavGraphBuilder.homeDestination(onNavigateToDetail: (String) -> Unit) {
    composable<Routes.Home> { HomeRoute(onNavigateToDetail = onNavigateToDetail) }
}

private fun NavGraphBuilder.detailDestination(onNavigateBack: () -> Unit) {
    composable<Routes.Detail> { entry ->
        DetailRoute(nasaId = entry.toRoute<Routes.Detail>().nasaId, onNavigateBack = onNavigateBack)
    }
}
