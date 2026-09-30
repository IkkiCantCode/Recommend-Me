package com.ikki.recommendme.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ikki.recommendme.domain.model.MediaType
import com.ikki.recommendme.ui.auth.SignInScreen
import com.ikki.recommendme.ui.auth.SignUpScreen
import com.ikki.recommendme.ui.detail.DetailScreen
import com.ikki.recommendme.ui.home.HomeScreen
import com.ikki.recommendme.ui.profile.ProfileScreen
import com.ikki.recommendme.ui.questionnaire.QuestionnaireScreen
import com.ikki.recommendme.ui.search.SearchScreen
import com.ikki.recommendme.ui.watchlist.WatchlistScreen

object Routes {
    const val SIGN_IN = "signin"
    const val SIGN_UP = "signup"
    const val QUESTIONNAIRE = "questionnaire"
    const val HOME = "home"
    const val SEARCH = "search"
    const val WATCHLIST = "watchlist"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{id}/{type}"

    fun detail(id: Int, mediaType: MediaType): String = "detail/$id/${mediaType.name}"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomTab(Routes.SEARCH, "Search", Icons.Filled.Search),
    BottomTab(Routes.WATCHLIST, "Watchlist", Icons.Filled.Bookmarks),
    BottomTab(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

@Composable
fun AppNavHost(startDestination: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(tab.icon, contentDescription = tab.label)
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SIGN_IN) { SignInScreen(navController) }
            composable(Routes.SIGN_UP) { SignUpScreen(navController) }
            composable(Routes.QUESTIONNAIRE) { QuestionnaireScreen(navController) }
            composable(Routes.HOME) { HomeScreen(navController) }
            composable(Routes.SEARCH) { SearchScreen(navController) }
            composable(Routes.WATCHLIST) { WatchlistScreen(navController) }
            composable(Routes.PROFILE) { ProfileScreen(navController) }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: return@composable
                val typeName = entry.arguments?.getString("type") ?: MediaType.MOVIE.name
                runCatching { MediaType.valueOf(typeName) }
                    .getOrDefault(MediaType.MOVIE)
                    .let { type -> DetailScreen(id, type, navController) }
            }
        }
    }
}
