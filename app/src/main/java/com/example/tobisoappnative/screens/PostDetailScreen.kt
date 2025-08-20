package com.example.tobisoappnative.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tobisoappnative.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import coil.compose.AsyncImage
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val postDetail by viewModel.postDetail.collectAsState()
    val postDetailError by viewModel.postDetailError.collectAsState()
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(postDetail?.title ?: "Detail příspěvku") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            postDetailError != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Chyba při načítání příspěvku: ${postDetailError}", color = MaterialTheme.colorScheme.error)
                }
            }
            !loaded || postDetail == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = postDetail?.title ?: "", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    postDetail?.content?.let { content ->
                        val fixedContent = content.replace(Regex("!\\[(.*?)]\\((images/[^)]+)\\)")) {
                            val alt = it.groupValues[1]
                            val path = it.groupValues[2]
                            "![${alt}](https://tobiso.com/${path})"
                        }
                        RichText {
                            Markdown(fixedContent)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Vytvořeno: ${postDetail?.createdAt}", style = MaterialTheme.typography.bodySmall)
                    postDetail?.updatedAt?.let {
                        Text(text = "Upraveno: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    postDetail?.filePath.takeIf { !it.isNullOrBlank() }?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
