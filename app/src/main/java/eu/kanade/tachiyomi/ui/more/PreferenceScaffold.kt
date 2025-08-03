package eu.kanade.tachiyomi.ui.more

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import yokai.presentation.component.AppBar
import yokai.presentation.core.components.Scaffold
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import yokai.presentation.component.preference.Preference
import yokai.presentation.settings.PreferenceScreen
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

@Composable
fun PreferenceScaffold(
    titleRes: StringResource,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(titleRes),
                navigateUp = onBackPressed,
                actions = actions,
                scrollBehavior = it,
            )
        },
        content = { contentPadding ->
            PreferenceScreen(
                items = itemsProvider(),
                listState = listState,
                contentPadding = contentPadding,
            )
        },
    )
}
