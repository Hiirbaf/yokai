package yokai.presentation.extension.repo

import android.content.Context
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import yokai.domain.category.model.Category
import yokai.i18n.MR
import dev.icerock.moko.resources.getString

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
