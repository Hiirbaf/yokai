package eu.kanade.presentation.category

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.kanade.tachiyomi.data.database.models.Category
import yokai.i18n.MR
import dev.icerock.moko.resources.compose.stringResource

val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.stringResource(MR.strings.label_default)
        else -> name
    }
