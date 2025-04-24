package yokai.presentation.extension.repo

import android.content.Context
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.toLocalizedString
import yokai.domain.category.model.Category
import yokai.i18n.MR

fun Context.localizedString(resource: StringResource, vararg args: Any): String {
    return StringDesc.Resource(resource, args.toList()).toLocalizedString(this)
}

val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.localizedString(MR.strings.label_default)
        else -> name
    }
