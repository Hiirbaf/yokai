package yokai.presentation.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import yokai.presentation.component.AppBar
import androidx.compose.material3.Scaffold
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun PreferenceScaffold(
    titleRes: StringResource,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(titleRes),
                navigateUp = onBackPressed,
                actions = actions,
                //scrollBehavior = it,
            )
        },
        content = { contentPadding ->
            PreferenceScreen(
                items = itemsProvider(),
                contentPadding = contentPadding,
            )
        },
    )
}
