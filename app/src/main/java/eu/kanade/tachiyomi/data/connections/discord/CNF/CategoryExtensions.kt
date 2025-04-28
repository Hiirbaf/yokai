package eu.kanade.presentation.category

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.kanade.tachiyomi.data.database.models.Category
import yokai.i18n.MR
import dev.icerock.moko.resources.compose.stringResource

// Composable version
val Category.visualName: String
    @Composable
    get() = when {
        isDefaultSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

// Non-Composable version that accepts a Context
fun Category.visualName(context: Context): String =
    when {
        isDefaultSystemCategory -> context.getString(MR.strings.label_default.resourceId)
        else -> name
    }
