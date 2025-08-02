package yokai.util

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import yokai.presentation.theme.SecondaryItemAlpha

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SecondaryItemAlpha)

fun Modifier.clickableNoIndication(
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        onLongClick = onLongClick,
        onClick = onClick,
    )
}
