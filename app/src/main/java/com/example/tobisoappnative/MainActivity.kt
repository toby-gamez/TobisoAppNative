package com.example.tobisoappnative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tobisoappnative.ui.theme.TobisoAppNativeTheme
import com.example.tobisoappnative.screens.HomeScreen
import com.example.tobisoappnative.screens.SearchScreen
import com.example.tobisoappnative.screens.MoreScreen
import com.example.tobisoappnative.navigation.BottomBar
import com.example.tobisoappnative.screens.CategoryListScreen
import com.example.tobisoappnative.screens.FeedbackScreen
import com.example.tobisoappnative.screens.AboutScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tobiso()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tobiso() {
    TobisoAppNativeTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val route = navBackStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = route == null ||
                            !(route.startsWith("postDetail") ||
                                    route.startsWith("about") ||
                                    route.startsWith("feedback")),
                            enter = slideInVertically(initialOffsetY = { it }), // slide-up
                    exit = slideOutVertically(targetOffsetY = { it })   // slide-down
                ) {
                    BottomBar(navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(
                    "home",
                ) {
                    HomeScreen(navController = navController)
                }
                composable(
                    "search",
                ) {
                    SearchScreen(navController = navController)
                }
                composable(
                    "more",
                ) {
                    MoreScreen(navController = navController)
                }
                composable(
                    "feedback",
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400))
                    }
                ) {
                    FeedbackScreen(navController = navController)
                }
                composable(
                    "about",
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400))
                    }
                ) {
                    AboutScreen(navController = navController)
                }
                composable("categoryList/{categoryName}") { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                    CategoryListScreen(parentCategoryName = categoryName, navController = navController)
                }
                composable(
                    "postDetail/{postId}",
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400))
                    }
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull()
                    if (postId != null) {
                        com.example.tobisoappnative.screens.PostDetailScreen(postId = postId, navController = navController)
                    } else {
                        Text("Chybný postId", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
