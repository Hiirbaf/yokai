package yokai.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.domain.manga.models.Manga
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    initialQuery: String,
    onBackPress: () -> Unit,
    onResultClick: (Manga) -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var debouncedQuery by remember { mutableStateOf(initialQuery) }
    var active by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Debouncing the input query
    LaunchedEffect(searchQuery) {
        delay(400) // 400ms delay to prevent spamming APIs
        debouncedQuery = searchQuery
        if (searchQuery.isNotEmpty() && !active) {
            active = true
        }
    }

    // Auto-focus keyboard on launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { 
                    keyboardController?.hide()
                    active = false 
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search Library & Global Sources...") },   
                leadingIcon = {
                    IconButton(onClick = onBackPress) {
                        if (active) Icon(Icons.Filled.ArrowBack, contentDescription = "Back") else Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            ) {
                // Pre-Search States
                if (searchQuery.isEmpty()) {
                    Text("Search History", modifier =Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    // TODO: Loop over actual search history items here
                    
                    Text("Category/Genre Tags", modifier =Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    // TODO: Display actionable chips for Action, Seinen, Sci-fi
                } else {
                    Text("Real-Time Suggestions For: $debouncedQuery", modifier =Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    // TODO: Shimmer effect & parallel results
                }
            }
        }
    ) { padding ->
        // Multi-Source Integration Results
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item {
                if (debouncedQuery.isNotEmpty()) {
                     Text("Search Results for $debouncedQuery", modifier = Modifier.padding(16.dp))
                }
    }
}

object GlobalSearchScreen {
    const val INITIAL_QUERY = "initial_query"
}
