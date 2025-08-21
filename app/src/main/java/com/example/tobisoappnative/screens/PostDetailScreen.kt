package com.example.tobisoappnative.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import com.example.tobisoappnative.model.ApiClient
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    var showError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
        loaded = true
    }

    // ✅ Odstraněn Scaffold - padding se aplikuje z MainActivity
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LargeTopAppBar(
            title = { Text(postDetail?.title ?: "Detail příspěvku") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                }
            }
        )

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
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    postDetail?.content?.let { content ->
                        val fixedContent = content.replace(Regex("!\\[(.*?)]\\((images/[^)]+)\\)")) {
                            val alt = it.groupValues[1]
                            val path = it.groupValues[2]
                            "![${alt}](https://tobiso.com/${path})"
                        }
                        // Regex pro odkazy v Markdownu
                        val linkRegex = Regex("\\[(.+?)\\]\\((.+?)\\)")
                        val matches = linkRegex.findAll(fixedContent).toList()
                        if (matches.isEmpty()) {
                            RichText { Markdown(fixedContent) }
                        } else {
                            var lastIndex = 0
                            Column {
                                for (match in matches) {
                                    val start = match.range.first
                                    val end = match.range.last + 1
                                    // Text před odkazem
                                    if (start > lastIndex) {
                                        val before = fixedContent.substring(lastIndex, start)
                                        RichText { Markdown(before) }
                                    }
                                    // Odkaz
                                    val linkText = match.groupValues[1]
                                    val url = match.groupValues[2]
                                    var fileName = url
                                    if (fileName.endsWith(".html")) fileName = fileName.removeSuffix(".html") + ".md"
                                    fileName = fileName.replace(prefixRegex, "")
                                    if (!fileName.startsWith("/")) fileName = "/$fileName"
                                    ClickableText(
                                        text = AnnotatedString(linkText),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                                        onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    val postsApi = ApiClient.apiService.getPosts()
                                                    val post = postsApi.find { it.filePath == fileName }
                                                    if (post != null) {
                                                        navController.navigate("postDetail/${post.id}")
                                                        showError = false
                                                    } else {
                                                        errorText = "Soubor '$fileName' nebyl nalezen."
                                                        showError = true
                                                    }
                                                } catch (e: Exception) {
                                                    errorText = "Chyba při načítání postů: ${e.message}"
                                                    showError = true
                                                }
                                            }
                                        }
                                    )

                                    lastIndex = end
                                }
                                // Zbytek textu za posledním odkazem
                                if (lastIndex < fixedContent.length) {
                                    val after = fixedContent.substring(lastIndex)
                                    RichText { Markdown(after) }
                                }
                                // Snackbar mimo cyklus, zobrazí se pouze jednou
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val locale = java.util.Locale("cs", "CZ")
                    val formatter = SimpleDateFormat("dd. MM. yyyy 'v' HH:mm", locale)
                    val createdFormatted = postDetail?.createdAt?.let {
                        try {
                            formatter.format(it)
                        } catch (e: Exception) {
                            ""
                        }
                    } ?: ""
                    val updatedFormatted = postDetail?.updatedAt?.let {
                        try {
                            formatter.format(it)
                        } catch (e: Exception) {
                            ""
                        }
                    } ?: ""
                    Text(text = "Vytvořeno: $createdFormatted", style = MaterialTheme.typography.bodySmall)
                    if (updatedFormatted.isNotBlank()) {
                        Text(text = "Upraveno: $updatedFormatted", style = MaterialTheme.typography.bodySmall)
                    }
                    postDetail?.filePath.takeIf { !it.isNullOrBlank() }?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Přesun Snackbar mimo Column, aby byl vždy viditelný
        if (showError) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showError = false }) { Text("Zavřít") }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(errorText)
                }
            }
        }
    }
}

val prefixRegex = Regex("^(ml-|sl-|li-|hv-|m-|ch-|f-|pr-|z-)")
