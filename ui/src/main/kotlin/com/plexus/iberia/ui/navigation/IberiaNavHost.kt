package com.plexus.iberia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plexus.iberia.ui.detail.DetailScreen
import com.plexus.iberia.ui.home.HomeScreen
import com.plexus.iberia.ui.splash.SplashScreen

@Composable
internal fun IberiaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Splash,
        modifier = modifier,
    ) {
        splashDestination(onFinished = navController::navigateToHome)
        homeDestination()
        detailDestination()
    }
}

private fun NavGraphBuilder.splashDestination(onFinished: () -> Unit) {
    composable<Destination.Splash> { SplashScreen(onFinished = onFinished) }
}

private fun NavGraphBuilder.homeDestination() {
    composable<Destination.Home> { HomeScreen() }
}

private fun NavGraphBuilder.detailDestination() {
    composable<Destination.Detail> { DetailScreen() }
}
