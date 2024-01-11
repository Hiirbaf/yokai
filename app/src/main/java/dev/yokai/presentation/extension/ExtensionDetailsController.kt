package dev.yokai.presentation.extension

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import dev.yokai.presentation.theme.YokaiTheme
import eu.kanade.tachiyomi.ui.base.controller.BaseController

class ExtensionDetailsController(bundle: Bundle? = null) : BaseController(bundle) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        return ComposeView(container.context).apply {
            setContent {
                YokaiTheme {
                    // TODO
                    Text(text = "Hello World")
                }
            }
        }
    }
}
