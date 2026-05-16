package com.skillexchange.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.skillexchange.ui.screens.*
import com.skillexchange.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    object Auth          : Screen("auth")
    object PhoneAuth    : Screen("phone_auth")
    object ProfileSetup : Screen("profile_setup")
    object Feed         : Screen("feed")
    object Post         : Screen("post")
    object Swaps        : Screen("swaps")
    object Profile      : Screen("profile")
    object Leaderboard  : Screen("leaderboard")
    object Terms        : Screen("terms")
    object Village      : Screen("village")
    object PhoneReset   : Screen("phone_reset")
    object Detail       : Screen("detail/{postId}") {
        fun go(id: String) = "detail/$id"
    }
    object Chat         : Screen("chat/{swapId}") {
        fun go(id: String) = "chat/$id"
    }
}

@Composable
fun NavGraph(navController: NavHostController, vm: AppViewModel) {
    val start = if (vm.currentUser != null) Screen.Feed.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = start) {

        composable(Screen.Auth.route) {
            AuthScreen(
                vm = vm,
                onAuthSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onSetupRequired = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onPhoneResetClick = { navController.navigate(Screen.PhoneReset.route) },
                onTermsClick = { navController.navigate(Screen.Terms.route) }
            )
        }

        composable(Screen.PhoneReset.route) {
            PhoneResetScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Screen.Terms.route) {
            TermsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Village.route) {
            VillageProfileScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Screen.PhoneAuth.route) {
            PhoneAuthScreen(
                vm = vm,
                onNewUser = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.PhoneAuth.route) { inclusive = true }
                    }
                },
                onExistingUser = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.PhoneAuth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(vm = vm, onDone = {
                navController.navigate(Screen.Feed.route) {
                    popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                vm = vm,
                onPostClick = { 
                    if (it == "NEW_POST") navController.navigate(Screen.Post.route)
                    else navController.navigate(Screen.Detail.go(it)) 
                },
                onLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                onProfile = { navController.navigate(Screen.Profile.route) },
                onSwaps = { navController.navigate(Screen.Swaps.route) },
                onVillage = { navController.navigate(Screen.Village.route) },
                onTerms = { navController.navigate(Screen.Terms.route) },
                onLogout = {
                    vm.logout {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Post.route) {
            PostScreen(vm = vm, onDone = {
                navController.navigate(Screen.Feed.route) { launchSingleTop = true }
            })
        }

        composable(Screen.Swaps.route) {
            SwapsScreen(
                vm = vm,
                onChat = { navController.navigate(Screen.Chat.go(it)) },
                onHome = { navController.navigate(Screen.Feed.route) { launchSingleTop = true } },
                onLeaderboard = { navController.navigate(Screen.Leaderboard.route) { launchSingleTop = true } },
                onProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                vm = vm,
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Screen.Feed.route) { launchSingleTop = true } },
                onSwaps = { navController.navigate(Screen.Swaps.route) { launchSingleTop = true } },
                onLeaderboard = { navController.navigate(Screen.Leaderboard.route) { launchSingleTop = true } }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onHome = { navController.navigate(Screen.Feed.route) { launchSingleTop = true } },
                onSwaps = { navController.navigate(Screen.Swaps.route) { launchSingleTop = true } },
                onProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } }
            )
        }

        composable(
            Screen.Detail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { back ->
            PostDetailScreen(
                vm = vm,
                postId = back.arguments?.getString("postId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.Chat.route,
            arguments = listOf(navArgument("swapId") { type = NavType.StringType })
        ) { back ->
            ChatScreen(
                vm = vm,
                swapId = back.arguments?.getString("swapId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
