package dev.simonsickle.flux.ui

import android.net.Uri
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

    fun player(
        url: String,
        contentId: String = "",
        contentType: String = "",
        title: String = "",
        poster: String? = null
    ): String {
        val encodedUrl = Uri.encode(url)
        val encodedTitle = Uri.encode(title)
        val encodedPoster = poster?.let { Uri.encode(it) } ?: ""
        return "player?url=$encodedUrl&contentId=$contentId&contentType=$contentType&title=$encodedTitle&poster=$encodedPoster"
    }
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
                onNavigateToPlayer = { streamUrl, contentId, contentType, title, poster ->
                    navController.navigate(
                        FluxRoutes.player(streamUrl, contentId, contentType, title, poster)
                    )
                }
            )
        }
        composable(
            route = "${FluxRoutes.PLAYER}?url={url}&contentId={contentId}&contentType={contentType}&title={title}&poster={poster}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType; defaultValue = "" },
                navArgument("contentId") { type = NavType.StringType; defaultValue = "" },
                navArgument("contentType") { type = NavType.StringType; defaultValue = "" },
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("poster") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            PlayerRoute(
                streamUrl = "",  // PlayerViewModel reads from SavedStateHandle
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
