package com.example.tobisoappnative.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tobisoappnative.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PostListScreen(
    categoryId: Int? = null,
    viewModel: MainViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    LaunchedEffect(categoryId) {
        viewModel.loadPosts(categoryId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Seznam příspěvků") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(posts) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

