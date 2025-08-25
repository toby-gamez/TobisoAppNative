package com.example.tobisoappnative.screens

import androidx.compose.foundation.background
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
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.AnnotatedString
import com.example.tobisoappnative.model.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign

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
                        // Nejprve nahradíme obrázky
                        val imageRegex = Regex("!\\[(.*?)]\\((images/[^)]+)\\)")
                        var processedContent = content.replace(imageRegex) {
                            val alt = it.groupValues[1]
                            val path = it.groupValues[2]
                            "![${alt}](https://tobiso.com/${path})"
                        }

                        // Najdeme zvýrazněné bloky ...text...
                        val blockRegex = Regex("\\.\\.\\.\\s*([\\s\\S]*?)\\s*\\.\\.\\.")
                        val blockMatches = blockRegex.findAll(processedContent).toList()

                        // Najdeme odkazy [text](url) ale ne obrázky ![alt](url)
                        val linkRegex = Regex("(?<!!)\\[(.+?)\\]\\((.+?)\\)")
                        val linkMatches = linkRegex.findAll(processedContent).toList()

                        // Kombinujeme všechny matches a seřadíme podle pozice
                        val allMatches = (blockMatches.map {
                            Triple(it.range.first, it.range.last + 1, "block" to it)
                        } + linkMatches.map {
                            Triple(it.range.first, it.range.last + 1, "link" to it)
                        }).sortedBy { it.first }

                        if (allMatches.isEmpty()) {
                            SelectionContainer {
                                RichText { Markdown(processedContent) }
                            }
                        } else {
                            var lastIndex = 0
                            Column {
                                for ((start, end, typeAndMatch) in allMatches) {
                                    // Text před aktuálním elementem
                                    if (start > lastIndex) {
                                        val before = processedContent.substring(lastIndex, start)
                                        SelectionContainer {
                                            RichText { Markdown(before) }
                                        }
                                    }

                                    when (typeAndMatch.first) {
                                        "block" -> {
                                            // Zvýrazněný blok
                                            val match = typeAndMatch.second as MatchResult
                                            val blockText = match.groupValues[1]
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        shape = MaterialTheme.shapes.medium
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                SelectionContainer {
                                                    RichText { Markdown(blockText) }
                                                }
                                            }
                                        }
                                        "link" -> {
                                            // Klikatelný odkaz
                                            val match = typeAndMatch.second as MatchResult
                                            val linkText = match.groupValues[1]
                                            val url = match.groupValues[2]
                                            var fileName = url
                                            if (fileName.endsWith(".html")) fileName = fileName.removeSuffix(".html") + ".md"
                                            fileName = fileName.replace(prefixRegex, "")
                                            if (!fileName.startsWith("/")) fileName = "/$fileName"
                                            ClickableText(
                                                text = AnnotatedString(linkText),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                ),
                                                onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            val postsApi = ApiClient.apiService.getPosts()
                                                            val post = postsApi.find { it.filePath == fileName }
                                                            if (post != null) {
                                                                navController.navigate("postDetail/${post.id}")
                                                                showError = false
                                                            } else {
                                                                if (url.contains("http")) {
                                                                    // Otevřít v prohlížeči
                                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                    try {
                                                                        navController.context.startActivity(intent)
                                                                        showError = false
                                                                    } catch (e: Exception) {
                                                                        errorText = "Nelze otevřít odkaz: ${e.message}"
                                                                        showError = true
                                                                    }
                                                                } else {
                                                                    errorText = "Soubor '$fileName' nebyl nalezen."
                                                                    showError = true
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            errorText = "Chyba při načítání postů: ${e.message}"
                                                            showError = true
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    lastIndex = end
                                }

                                // Zbytek textu za posledním elementem
                                if (lastIndex < processedContent.length) {
                                    val after = processedContent.substring(lastIndex)
                                    SelectionContainer {
                                        RichText { Markdown(after) }
                                    }
                                }
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
                    Text(text = "Vytvořeno: $createdFormatted", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start)
                    if (updatedFormatted.isNotBlank()) {
                        Text(text = "Upraveno: $updatedFormatted", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start)
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
                        TextButton(onClick = { showError = false }) { Text("Zavřít", textAlign = TextAlign.Start) }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(errorText, textAlign = TextAlign.Start)
                }
            }
        }
    }
}

val prefixRegex = Regex("^(ml-|sl-|li-|hv-|m-|ch-|f-|pr-|z-)")