package com.example.tobisoappnative

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.tobisoappnative.screens.ChangelogScreen
import com.example.tobisoappnative.screens.NoInternetScreen
import com.example.tobisoappnative.viewmodel.MainViewModel
import kotlinx.coroutines.delay

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val isConnected = remember { mutableStateOf(checkInternetConnection(context)) }
    val mainViewModel: MainViewModel = viewModel()
    val categories by mainViewModel.categories.collectAsState()
    val categoryError by mainViewModel.categoryError.collectAsState()

    // Periodická kontrola connectivity pomocí delay
    LaunchedEffect(context) {
        while (true) {
            isConnected.value = checkInternetConnection(context)
            kotlinx.coroutines.delay(2000)
        }
    }

    // Callback pro ruční obnovu
    val onRetry = {
        isConnected.value = checkInternetConnection(context)
    }

    // Timeout stav
    val loadingTimeout = remember { mutableStateOf(false) }
    LaunchedEffect(isConnected.value) {
        if (isConnected.value) {
            loadingTimeout.value = false
        } else {
            loadingTimeout.value = false
            kotlinx.coroutines.delay(30000)
            if (!isConnected.value && categories.isEmpty() && categoryError == null) {
                loadingTimeout.value = true
            }
        }
    }

    // Načtení kategorií při startu
    LaunchedEffect(Unit) {
        mainViewModel.loadCategories()
    }

    TobisoAppNativeTheme {
        val searchRequestFocus = remember { mutableStateOf(false) }
        // Zobrazit toast s kolečkem, pokud není připojení nebo nejsou načteny kategorie
        if (!isConnected.value) {
            NoInternetScreen(onRetry = onRetry)
        } else if (categories.isEmpty() && categoryError == null) {
            LoadingToast(timeout = loadingTimeout.value)
        } else {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val route = navBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    AnimatedVisibility(
                        visible = route == null ||
                                !(route.startsWith("postDetail") ||
                                        route.startsWith("about") ||
                                        route.startsWith("feedback") ||
                                        route.startsWith("changelog")),
                        enter = slideInVertically(initialOffsetY = { it }), // slide-up
                        exit = slideOutVertically(targetOffsetY = { it })   // slide-down
                    ) {
                        BottomBar(navController, searchRequestFocus)
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
                        SearchScreen(navController = navController, searchRequestFocus = searchRequestFocus)
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
                        "changelog",
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
                        ChangelogScreen(navController = navController)
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
}

@Composable
fun LoadingToast(timeout: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)), // tmavší overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(32.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (timeout) "Připojení k serveru trvá déle než 30 sekund" else "Čekám na připojení k serveru...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
