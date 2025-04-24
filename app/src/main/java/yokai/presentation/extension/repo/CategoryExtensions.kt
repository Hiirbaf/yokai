package yokai.presentation.extension.repo

import android.content.Context
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.getString
import androidx.compose.runtime.Composable

fun Context.localizedString(resource: StringResource, vararg args: Any): String {
    return StringDesc.ResourceFormatted(resource, *args).toString(this)
}

// Versión para Context
fun Context.localizedString(resource: StringResource, vararg args: Any): String {
    return resource.getString(this, *args)
}

// Versión Composable
@Composable
fun localizedString(resource: StringResource, vararg args: Any): String {
    return stringResource(resource, *args)
}
val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> localizedString(MR.strings.label_default, name)
        else -> name
    }

fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.localizedString(MR.strings.label_default, name)
        else -> name
    }
