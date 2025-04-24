package yokai.presentation.more.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import yokai.presentation.extension.repo.visualName
import yokai.domain.category.models.CategoryUpdate
import yokai.i18n.MR
import dev.icerock.moko.resources.stringResource

/**
 * Returns a string of categories name for settings subtitle
 */
@ReadOnlyComposable
@Composable
fun getCategoriesLabel(
    allCategories: List<CategoryUpdate>,
    included: Set<String>,
    excluded: Set<String>,
): String {
    val context = LocalContext.current

    val includedCategories = included
        .mapNotNull { id -> allCategories.find { it.id == id.toLong() } }
        .sortedBy { it.order }
    val excludedCategories = excluded
        .mapNotNull { id -> allCategories.find { it.id == id.toLong() } }
        .sortedBy { it.order }
    val allExcluded = excludedCategories.size == allCategories.size

    val includedItemsText = when {
        // Some selected, but not all
        includedCategories.isNotEmpty() && includedCategories.size != allCategories.size ->
            includedCategories.joinToString { it.visualName(context) }
        // All explicitly selected
        includedCategories.size == allCategories.size -> stringResource(MR.strings.all)
        allExcluded -> stringResource(MR.strings.none)
        else -> stringResource(MR.strings.all)
    }
    val excludedItemsText = when {
        excludedCategories.isEmpty() -> stringResource(MR.strings.none)
        allExcluded -> stringResource(MR.strings.all)
        else -> excludedCategories.joinToString { it.visualName(context) }
    }
    return stringResource(MR.strings.include, includedItemsText) + "\n" +
        stringResource(MR.strings.exclude, excludedItemsText)
}

@ReadOnlyComposable
@Composable
fun getCategoriesLabel(
    allCategories: List<CategoryUpdate>,
    included: Set<String>,
): String {
    val context = LocalContext.current

    val includedCategories = included
        .mapNotNull { id -> allCategories.find { it.id == id.toLong() } }
        .sortedBy { it.order }

    val includedItemsText = when {
        // Some selected, but not all
        includedCategories.isNotEmpty() && includedCategories.size != allCategories.size -> includedCategories.joinToString { it.visualName(context) }
        // All explicitly selected
        includedCategories.size == allCategories.size -> stringResource(MR.strings.all)
        includedCategories.isEmpty() -> stringResource(MR.strings.none)
        else -> stringResource(MR.strings.all)
    }
    return stringResource(MR.strings.include, includedItemsText)
}
