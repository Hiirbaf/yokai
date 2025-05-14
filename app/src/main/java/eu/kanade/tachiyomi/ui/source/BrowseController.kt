package eu.kanade.tachiyomi.ui.source

import androidx.compose.foundation.layout.* import androidx.compose.foundation.lazy.LazyColumn import androidx.compose.foundation.lazy.items import androidx.compose.material3.* import androidx.compose.runtime.Composable import androidx.compose.runtime.getValue import androidx.compose.runtime.mutableStateOf import androidx.compose.runtime.remember import androidx.compose.runtime.setValue import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier import androidx.compose.ui.graphics.Color import androidx.compose.ui.res.stringResource import androidx.compose.ui.unit.dp import androidx.lifecycle.compose.collectAsStateWithLifecycle import cafe.adriel.voyager.core.model.coroutineScope import eu.kanade.presentation.components.Scaffold import eu.kanade.presentation.util.Screen import eu.kanade.tachiyomi.R import eu.kanade.tachiyomi.source.CatalogueSource import eu.kanade.tachiyomi.source.Source import eu.kanade.tachiyomi.ui.source.SourcePresenter import kotlinx.coroutines.flow.MutableStateFlow import kotlinx.coroutines.flow.StateFlow import kotlinx.coroutines.flow.asStateFlow import kotlinx.coroutines.launch

class BrowseScreen : Screen() {

@Composable
override fun Content() {
    val model = remember { BrowseScreenModel() }
    val state by model.state.collectAsStateWithLifecycle()

    BrowseScreen(
        state = state,
        onSourceClick = model::onSourceClick,
        onLatestClick = model::onLatestClick,
        onPinClick = model::onPinClick,
        onHideClick = model::onHideClick,
        onSearchClick = model::onSearchClick,
    )
}

}

@Composable fun BrowseScreen( state: BrowseScreenState, onSourceClick: (Source) -> Unit, onLatestClick: (Source) -> Unit, onPinClick: (Source) -> Unit, onHideClick: (Source) -> Unit, onSearchClick: () -> Unit, modifier: Modifier = Modifier, ) { Scaffold( topBar = { TopAppBar( title = { Text(text = stringResource(R.string.browse)) }, actions = { IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, contentDescription = null) } } ) }, modifier = modifier ) { padding -> LazyColumn( contentPadding = PaddingValues( start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(), bottom = 96.dp ), verticalArrangement = Arrangement.spacedBy(8.dp) ) { state.items.forEach { (header, sources) -> item { Text( text = header, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp) ) } items(sources) { source -> SourceItem( source = source, onClick = { onSourceClick(source) }, onLatestClick = { onLatestClick(source) }, onPinClick = { onPinClick(source) }, onHideClick = { onHideClick(source) }, ) } } } } }

@Composable fun SourceItem( source: Source, onClick: () -> Unit, onLatestClick: () -> Unit, onPinClick: () -> Unit, onHideClick: () -> Unit, ) { Card( onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), ) { Row( verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp) ) { Column(modifier = Modifier.weight(1f)) { Text(source.name, style = MaterialTheme.typography.bodyLarge) if (source is CatalogueSource) { Text(source.lang.uppercase(), style = MaterialTheme.typography.bodySmall) } } IconButton(onClick = onLatestClick) { Icon(Icons.Default.Refresh, contentDescription = "Latest") } IconButton(onClick = onPinClick) { Icon(Icons.Default.PushPin, contentDescription = "Pin") } IconButton(onClick = onHideClick) { Icon(Icons.Default.VisibilityOff, contentDescription = "Hide") } } } }

data class BrowseScreenState( val items: List<Pair<String, List<Source>>> = emptyList(), )

class BrowseScreenModel { private val presenter = SourcePresenter(null) // inyecta si usas Koin/Injekt private val _state = MutableStateFlow(BrowseScreenState()) val state: StateFlow<BrowseScreenState> = _state.asStateFlow()

init {
    presenter.onCreate()
    updateSources()
}

fun onSourceClick(source: Source) {
    // Navegar a catálogo
}

fun onLatestClick(source: Source) {
    // Navegar a catálogo latest
}

fun onPinClick(source: Source) {
    // Alternar pin y actualizar
}

fun onHideClick(source: Source) {
    // Ocultar fuente y actualizar
}

fun onSearchClick() {
    // Navegar a global search
}

fun updateSources() {
    val items = presenter.sourceItems
        .filterIsInstance<SourceItem>()
        .groupBy { it.source.lang } // o usar headers existentes
        .map { (lang, list) ->
            lang.uppercase() to list.map { it.source }
        }

    _state.value = BrowseScreenState(items)
}

}

