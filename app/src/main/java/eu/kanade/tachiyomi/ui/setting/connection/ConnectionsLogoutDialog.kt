package eu.kanade.tachiyomi.ui.setting.connections

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.service.ConnectionsService
import eu.kanade.tachiyomi.util.system.toast

class ConnectionsLogoutDialog(
    private val service: ConnectionsService
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(R.string.logout) { _, _ ->
                service.logout()
                requireContext().toast(R.string.logout_success)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}
