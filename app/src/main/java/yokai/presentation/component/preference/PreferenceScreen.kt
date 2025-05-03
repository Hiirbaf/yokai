package yokai.presentation.component.preference

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PreferenceScreen(
    items: ImmutableList<Preference>,
    highlightKey: String? = null,
) {
    LazyColumn {
        items(items) { pref ->
            when (pref) {
                is Preference.PreferenceGroup -> {
                    Text(
                        text = pref.title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    items(pref.preferenceItems) { item ->
                        PreferenceItem(item = item, highlightKey = highlightKey)
                    }
                }

                is Preference.PreferenceItem<*> -> {
                    PreferenceItem(item = pref, highlightKey = highlightKey)
                }
            }
        }
    }
}
