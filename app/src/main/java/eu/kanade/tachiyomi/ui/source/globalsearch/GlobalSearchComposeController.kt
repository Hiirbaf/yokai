package eu.kanade.tachiyomi.ui.source.globalsearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import eu.kanade.tachiyomi.ui.base.controller.BaseController
import yokai.presentation.theme.YokaiTheme
import yokai.presentation.search.GlobalSearchScreen

import yokai.presentation.search.GlobalSearchScreenConstants

class GlobalSearchComposeController(bundle: Bundle? = null) : BaseController(bundle) {

    override val shouldHideLegacyAppBar = true

    constructor(initialQuery: String) : this(Bundle().apply {
        putString(GlobalSearchScreenConstants.INITIAL_QUERY, initialQuery)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val initialQuery = args.getString(GlobalSearchScreenConstants.INITIAL_QUERY) ?: ""
        return ComposeView(inflater.context).apply {
            setContent {
                YokaiTheme {
                    GlobalSearchScreen(
                        initialQuery = initialQuery,
                        onBackPress = { router.popCurrentController() },
                        onResultClick = { manga -> 
                            // Route to MangaDetailsController etc.
                        }
                    )
                }
            }
        }
    }
}
