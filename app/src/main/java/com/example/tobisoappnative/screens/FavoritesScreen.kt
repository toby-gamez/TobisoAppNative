package com.example.tobisoappnative.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tobisoappnative.viewmodel.MainViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.tobisoappnative.model.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val favoritePosts by viewModel.favoritePosts.collectAsState()
    val snippets by viewModel.snippets.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadSnippets()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LargeTopAppBar(
            title = { Text("Oblíbené") },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Útržky") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Příspěvky") }
            )
        }
        // Stav pro dialog potvrzení mazání
        var showDeleteDialog by remember { mutableStateOf(false) }
        var deleteType by remember { mutableStateOf(0) } // 0 = snippets, 1 = posts
        // Tlačítko pro smazání obsahu tabulky
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            when (selectedTab) {
                0 -> Button(
                    onClick = { showDeleteDialog = true; deleteType = 0 },
                    enabled = snippets.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Smazat všechny útržky",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Smazat všechny útržky", color = MaterialTheme.colorScheme.onErrorContainer)
                }
                1 -> Button(
                    onClick = { showDeleteDialog = true; deleteType = 1 },
                    enabled = favoritePosts.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Smazat všechny příspěvky",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Smazat všechny příspěvky", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
        // Dialog potvrzení
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Opravdu chcete smazat vše?") },
                text = { Text(if (deleteType == 0) "Tímto smažete všechny uložené útržky." else "Tímto smažete všechny oblíbené příspěvky.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (deleteType == 0) viewModel.clearSnippets() else viewModel.clearFavoritePosts()
                        showDeleteDialog = false
                    }) {
                        Text("Ano")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Ne")
                    }
                }
            )
        }
        when (selectedTab) {
            0 -> {
                if (snippets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Nemáte žádné útržky.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(snippets) { snippet ->
                            val postTitleState = produceState<String?>(initialValue = null, snippet.postId) {
                                if (snippet.postId != 0) {
                                    value = try {
                                        withContext(Dispatchers.IO) {
                                            ApiClient.apiService.getPost(snippet.postId).title
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else {
                                    value = null
                                }
                            }
                            val postTitle = postTitleState.value
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .let { mod ->
                                        if (snippet.postId != 0 && postTitle != null) mod.clickable {
                                            navController.navigate("postDetail/${snippet.postId}")
                                        } else mod
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                                        when {
                                            snippet.postId == 0 -> Text("Neznámý soubor", style = MaterialTheme.typography.titleSmall)
                                            postTitle == null -> Text("Načítám název...", style = MaterialTheme.typography.titleSmall)
                                            else -> Text(postTitle, style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = snippet.content, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Vytvořeno: " + java.text.SimpleDateFormat("dd. MM. yyyy 'v' HH:mm", java.util.Locale.forLanguageTag("cs-CZ")).format(snippet.createdAt),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeSnippet(snippet) },
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Odstranit útržek",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                if (favoritePosts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Nemáte žádné oblíbené příspěvky.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(favoritePosts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { navController.navigate("postDetail/${post.id}") },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Description, contentDescription = "Post", modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                                        val updated = post.updatedAt
                                        val formatted = updated?.let {
                                            try {
                                                val formatter = java.text.SimpleDateFormat("dd. MM. yyyy 'v' HH:mm", java.util.Locale.forLanguageTag("cs-CZ"))
                                                formatter.format(it)
                                            } catch (_: Exception) {
                                                ""
                                            }
                                        } ?: ""
                                        if (formatted.isNotBlank()) {
                                            Text(text = "Upraveno: $formatted", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.unsavePost(post.id) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Odebrat z oblíbených",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
