package com.example.elcapitalmarx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.elcapitalmarx.ui.theme.ElcapitalMarxTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElcapitalMarxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SearchScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class PageContent(val pageNumber: Int, val text: String)
data class SearchResult(val pageNumber: Int, val text: String, val sentenceIndex: Int)

enum class Language(
    val displayName: String, 
    @RawRes val resourceId: Int,
    @RawRes val quotesResourceId: Int,
    val translationDetails: String
) {
    ENGLISH(
        "English", 
        R.raw.capital_en, 
        R.raw.quotes_en,
        "Translated by Samuel Moore and Edward Aveling (1887)"
    ),
    SPANISH(
        "Español", 
        R.raw.capital_es, 
        R.raw.quotes_es,
        "Translated by Wenceslao Roces (1946)"
    ),
    GERMAN(
        "Deutsch", 
        R.raw.capital_de, 
        R.raw.quotes_de,
        "Original text by Karl Marx (1867)"
    )
}

fun String.removeAccents(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{M}"), "")
}

@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(Language.SPANISH) }
    var pages by remember { mutableStateOf<List<PageContent>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var currentQuote by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var selectedResult by remember { mutableStateOf<SearchResult?>(null) }

    // Load content and quotes when language changes
    LaunchedEffect(selectedLanguage) {
        try {
            // Load text content
            val inputStream = context.resources.openRawResource(selectedLanguage.resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val fullText = reader.use { it.readText() }
            
            pages = fullText.split("--- Page ")
                .filter { it.isNotBlank() }
                .mapNotNull { section ->
                    try {
                        val headerEnd = section.indexOf(" ---")
                        if (headerEnd != -1) {
                            val pageNum = section.substring(0, headerEnd).trim().toInt()
                            val content = section.substring(headerEnd + 4).trim()
                            PageContent(pageNum, content)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

            // Load random quote
            val quotesStream = context.resources.openRawResource(selectedLanguage.quotesResourceId)
            val quotesReader = BufferedReader(InputStreamReader(quotesStream))
            val quotes = quotesReader.use { it.readLines() }.filter { it.isNotBlank() }
            if (quotes.isNotEmpty()) {
                currentQuote = quotes.random()
            }
        } catch (e: Exception) {
            pages = emptyList()
            currentQuote = "Error loading content"
        }
    }

    val searchResults = remember(searchQuery, pages) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val normalizedQuery = searchQuery.removeAccents()
            pages.flatMap { page ->
                page.text.split(Regex("(?<=[.!?])\\s+"))
                    .mapIndexedNotNull { index, sentence ->
                         if (sentence.removeAccents().contains(normalizedQuery, ignoreCase = true)) {
                             SearchResult(page.pageNumber, sentence, index)
                         } else {
                             null
                         }
                    }
            }
        }
    }

    val isSearching = searchQuery.isNotBlank()

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Translations") },
            text = {
                Column {
                    Language.values().forEach { language ->
                        Text(
                            text = "${language.displayName}:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = language.translationDetails,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (selectedResult != null) {
        TranslationComparisonDialog(result = selectedResult!!) {
            selectedResult = null
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isSearching) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://images.fineartamerica.com/images/artworkimages/mediumlarge/2/karl-marx-19th-century-german-print-collector.jpg")
                    .crossfade(true)
                    .build(),
                contentDescription = "Karl Marx",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            // Improved Language Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Language.values().forEach { language ->
                    val isSelected = language == selectedLanguage
                    if (isSelected) {
                        Button(
                            onClick = { /* No-op */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(language.displayName)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedLanguage = language }
                        ) {
                            Text(language.displayName)
                        }
                    }
                }
            }

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search in Capital (${selectedLanguage.displayName})") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (isSearching) {
            Text(
                text = "Found ${searchResults.size} matches",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.Start)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
            ) {
                items(searchResults) { result ->
                    ResultItem(
                        result = result, 
                        searchQuery = searchQuery,
                        onClick = { selectedResult = result }
                    )
                }
            }
        } else {
             // Display quote when idle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\"$currentQuote\"",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "― Karl Marx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(
                    onClick = { showAboutDialog = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text("About Translations", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun TranslationComparisonDialog(result: SearchResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var translations by remember { mutableStateOf<Map<Language, String>>(emptyMap()) }

    LaunchedEffect(result) {
        val newTranslations = mutableMapOf<Language, String>()
        
        Language.values().forEach { language ->
            try {
                val inputStream = context.resources.openRawResource(language.resourceId)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val fullText = reader.use { it.readText() }
                
                val pageSections = fullText.split("--- Page ")
                val pageContent = pageSections
                    .filter { it.isNotBlank() }
                    .firstNotNullOfOrNull { section ->
                        try {
                            val headerEnd = section.indexOf(" ---")
                            if (headerEnd != -1) {
                                val pageNum = section.substring(0, headerEnd).trim().toInt()
                                if (pageNum == result.pageNumber) {
                                    section.substring(headerEnd + 4).trim()
                                } else null
                            } else null
                        } catch (e: Exception) { null }
                    }

                if (pageContent != null) {
                    val sentences = pageContent.split(Regex("(?<=[.!?])\\s+"))
                    if (result.sentenceIndex < sentences.size) {
                        newTranslations[language] = sentences[result.sentenceIndex]
                    } else {
                        newTranslations[language] = "[Text mismatch or index out of bounds]"
                    }
                } else {
                    newTranslations[language] = "[Page not found]"
                }

            } catch (e: Exception) {
                newTranslations[language] = "[Error loading text]"
            }
        }
        translations = newTranslations
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fragment Comparison (Page ${result.pageNumber})") },
        text = {
            LazyColumn {
                items(Language.values()) { language ->
                    val text = translations[language] ?: "Loading..."
                    
                    Text(
                        text = language.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ResultItem(result: SearchResult, searchQuery: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Page ${result.pageNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            val annotatedString = buildAnnotatedString {
                val text = result.text
                val normalizedText = text.removeAccents().lowercase()
                val normalizedQuery = searchQuery.removeAccents().lowercase()
                
                var currentIndex = 0
                while (currentIndex < text.length) {
                    val remainingNormalized = normalizedText.substring(currentIndex)
                    val matchRelativeIndex = remainingNormalized.indexOf(normalizedQuery)
                    
                    if (matchRelativeIndex == -1) {
                        append(text.substring(currentIndex))
                        break
                    }
                    
                    val matchIndex = currentIndex + matchRelativeIndex
                    append(text.substring(currentIndex, matchIndex))
                    
                    val endMatchIndex = (matchIndex + searchQuery.length).coerceAtMost(text.length)

                    withStyle(style = SpanStyle(background = Color.Yellow, color = Color.Black)) {
                        append(text.substring(matchIndex, endMatchIndex))
                    }
                    
                    currentIndex = endMatchIndex
                }
            }
            
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    ElcapitalMarxTheme {
        SearchScreen()
    }
}
