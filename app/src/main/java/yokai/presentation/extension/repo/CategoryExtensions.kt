package yokai.presentation.extension.repo

import android.content.Context
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import yokai.domain.category.model.Category
import yokai.i18n.MR

// Extensión para traducir un recurso a String en contexto Android
fun Context.localizedString(resource: StringResource, vararg args: Any): String {
    return dev.icerock.moko.resources.desc.StringDesc.Resource(resource, args.toList()).toString(this)
}

// Propiedad Compose para usar en UI
val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

// Función no-Compose para usar fuera de Compose
fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.localizedString(MR.strings.label_default)
        else -> name
    }
