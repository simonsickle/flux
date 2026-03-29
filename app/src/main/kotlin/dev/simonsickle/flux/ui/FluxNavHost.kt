package dev.simonsickle.flux.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.simonsickle.flux.feature.home.HomeRoute
import dev.simonsickle.flux.feature.detail.DetailRoute
import dev.simonsickle.flux.feature.player.PlayerRoute
import dev.simonsickle.flux.feature.search.SearchRoute
import dev.simonsickle.flux.feature.settings.SettingsRoute
import dev.simonsickle.flux.feature.addons.AddonsRoute

object FluxRoutes {
    const val HOME = "home"
    const val DETAIL = "detail/{type}/{id}"
    const val PLAYER = "player"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val ADDONS = "addons"

    fun detail(type: String, id: String) = "detail/$type/$id"
}

@Composable
fun FluxNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FluxRoutes.HOME,
        modifier = modifier
    ) {
        composable(FluxRoutes.HOME) {
            HomeRoute(
                onNavigateToDetail = { type, id ->
                    navController.navigate(FluxRoutes.detail(type, id))
                },
                onNavigateToSearch = { navController.navigate(FluxRoutes.SEARCH) },
                onNavigateToSettings = { navController.navigate(FluxRoutes.SETTINGS) },
                onNavigateToAddons = { navController.navigate(FluxRoutes.ADDONS) }
            )
        }
        composable(
            route = FluxRoutes.DETAIL,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: return@composable
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            DetailRoute(
                type = type,
                id = id,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPlayer = { streamUrl ->
                    navController.navigate("${FluxRoutes.PLAYER}?url=$streamUrl")
                }
            )
        }
        composable(
            route = "${FluxRoutes.PLAYER}?url={url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            PlayerRoute(
                streamUrl = url,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(FluxRoutes.SEARCH) {
            SearchRoute(
                onNavigateToDetail = { type, id ->
                    navController.navigate(FluxRoutes.detail(type, id))
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(FluxRoutes.SETTINGS) {
            SettingsRoute(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(FluxRoutes.ADDONS) {
            AddonsRoute(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
