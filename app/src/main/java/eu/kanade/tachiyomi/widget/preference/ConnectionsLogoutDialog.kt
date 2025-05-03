package eu.kanade.tachiyomi.widget.preference

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.util.lang.withUIContext
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.ui.base.controller.DialogController
import eu.kanade.tachiyomi.ui.base.controller.ControllerDialog

class ConnectionsLogoutDialog(
    private val service: ConnectionsService,
) : DialogController(), ControllerDialog {

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()

        return MaterialAlertDialogBuilder(activity)
            .setTitle(activity.getString(R.string.logout_title, activity.getString(service.nameRes())))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.logout) { _, _ ->
                service.logout()
                activity.toast(R.string.logout_success)
            }
            .create()
    }
}
