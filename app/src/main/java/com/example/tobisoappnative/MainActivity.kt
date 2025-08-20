package com.example.tobisoappnative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tobisoappnative.ui.theme.TobisoAppNativeTheme
import com.example.tobisoappnative.screens.HomeScreen
import com.example.tobisoappnative.screens.SearchScreen
import com.example.tobisoappnative.screens.MoreScreen
import com.example.tobisoappnative.navigation.BottomBar
import com.example.tobisoappnative.screens.CategoryListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    TobisoAppNativeTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { BottomBar(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") {
                    HomeScreen(navController = navController)
                }
                composable("search") { SearchScreen() }
                composable("more") { MoreScreen() }
                composable("categoryList/{categoryName}") { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                    CategoryListScreen(parentCategoryName = categoryName, navController = navController)
                }
                composable("postDetail/{postId}") { backStackEntry ->
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
