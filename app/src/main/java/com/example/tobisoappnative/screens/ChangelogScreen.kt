package com.example.tobisoappnative.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(navController: NavController) {
    val version1_5 = listOf(
        "text se již dá vybírat a kopírovat (včetně intra a článků)",
        "přidána podpora pro otevírání souborů",
    )
    val version1_4 = listOf(
        "opraveny tabulky",
        "opraveny intra a linky, přidána podpora pro weblinky",
        "opraveno číslo verze v android 'O aplikaci'",
        "opraven tmavý režim v některých elementech a obrazovkách",
        "upraveny popisy předmětů a vyhledávání",
    )
    val version1_3 = listOf(
        "přidán deník změn",
        "přidáno vyhledávání",
        "přidána obrazovka o 'Bez internetu' a načítání",
        "zfunkčněny odkazy ve článcích",
        "zfunkčněny intra článků",
    )
    val version1_2 = listOf(
        "přidán základ aplikace, nastavení všeho (list předmětů, navigace atd.)",
        "přidáno načítání kategorií a postů i jejich obsahu, načítání obrázků",
        "zabezpečené připojení s api",
        "přidán readme",
        "přidán lepší design",
        "oprava layoutů",
    )

    // ✅ Odstraněn Scaffold - padding se aplikuje z MainActivity
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LargeTopAppBar(
            title = { Text("Deník změn") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
        ) {
            val context = LocalContext.current

            val annotatedText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append("Github ")
                }
                pushStringAnnotation(tag = "URL", annotation = "https://github.com/toby-gamez/TobisoAppNative")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("zde")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText
                        .getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()
                        ?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // důležité pokud spouštíš z Contextu
                            context.startActivity(intent)
                        }
                }
            )
            Text("Verze 1.5", style = typography.headlineSmall, modifier = Modifier.padding(bottom = 4.dp))
            version1_5.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Text("• $item", style = typography.bodyLarge)
                }
            }
            Text("Verze 1.4", style = typography.headlineSmall, modifier = Modifier.padding(bottom = 4.dp))
            version1_4.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Text("• $item", style = typography.bodyLarge)
                }
            }
            Text("Verze 1.3", style = typography.headlineSmall, modifier = Modifier.padding(bottom = 4.dp))
            version1_3.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Text("• $item", style = typography.bodyLarge)
                }
            }
            Text("Verze 1.2", style = typography.headlineSmall, modifier = Modifier.padding(bottom = 4.dp))
            version1_2.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Text("• $item", style = typography.bodyLarge)
                }
            }
        }
    }
}