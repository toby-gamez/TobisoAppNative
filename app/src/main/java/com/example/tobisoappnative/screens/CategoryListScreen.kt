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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryListScreen(
    parentCategoryName: String,
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val categoryError by viewModel.categoryError.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadCategories() }

    var parentCategoryNameState by remember { mutableStateOf(parentCategoryName) }

    val parentCategory = categories.find { it.name == parentCategoryNameState }
    val filteredCategories = parentCategory?.let { parent ->
        categories.filter { it.parentId == parent.id }
    } ?: emptyList()

    val showConnectionError = parentCategoryNameState == "Mluvnice" && (parentCategory == null || filteredCategories.isEmpty())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kategorie: $parentCategoryNameState") }) }
    ) { innerPadding ->
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(filteredCategories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { parentCategoryNameState = category.name }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
