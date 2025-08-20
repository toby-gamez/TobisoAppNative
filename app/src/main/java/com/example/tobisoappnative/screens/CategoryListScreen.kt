package com.example.tobisoappnative.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tobisoappnative.model.Category
import com.example.tobisoappnative.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Folder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryListScreen(
    parentCategoryName: String,
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val categoryError by viewModel.categoryError.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val postError by viewModel.postError.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadCategories() }

    var parentCategoryNameState by remember { mutableStateOf(parentCategoryName) }

    val parentCategory = categories.find { it.name == parentCategoryNameState }
    val filteredCategories = parentCategory?.let { parent ->
        categories.filter { it.parentId == parent.id }
    } ?: emptyList()

    val showConnectionError = parentCategoryNameState == "Mluvnice" && (parentCategory == null || filteredCategories.isEmpty())

    // Načtení postů při změně parentCategory
    LaunchedEffect(parentCategory?.id) {
        parentCategory?.id?.let { viewModel.loadPosts(it) }
    }

    // ✅ Odstraněn Scaffold - padding se aplikuje z MainActivity
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LargeTopAppBar(
            title = { Text("$parentCategoryNameState") },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (showConnectionError) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Chyba připojení nebo žádné podkategorie dostupné.", color = MaterialTheme.colorScheme.error)
                    if (categoryError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Detail chyby:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                        Text(categoryError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else if (postError != null) {
            // Zobrazení chybové hlášky na celé obrazovce při chybě načítání postů
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = "Chyba při načítání postů:",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = postError ?: "Neznámá chyba",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCategories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { parentCategoryNameState = category.name }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Folder, contentDescription = "Kategorie", modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                val filteredPosts = parentCategory?.let { parent ->
                    posts.filter { it.categoryId == parent.id }
                } ?: emptyList()
                // Zobrazení postů ke kategorii
                if (filteredCategories.isEmpty() && filteredPosts.isEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (postError != null) {
                                Text(
                                    text = "Chyba při načítání postů: ${postError}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (categoryError != null) {
                                Text(
                                    text = "Chyba při načítání kategorií: ${categoryError}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                } else {
                    items(filteredPosts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { navController.navigate("postDetail/${post.id}") },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = "Post", modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                                    val updated = post.updatedAt
                                    val formatted = updated?.let {
                                        try {
                                            val formatter = java.text.SimpleDateFormat("dd. MM. yyyy 'v' HH:mm", java.util.Locale("cs", "CZ"))
                                            formatter.format(it)
                                        } catch (e: Exception) {
                                            ""
                                        }
                                    } ?: ""
                                    if (formatted.isNotBlank()) {
                                        Text(text = "Upraveno: $formatted", style = MaterialTheme.typography.bodySmall)
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