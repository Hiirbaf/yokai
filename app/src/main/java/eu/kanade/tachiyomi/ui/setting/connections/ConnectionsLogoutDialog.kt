package eu.kanade.tachiyomi.ui.setting.connections

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.ui.base.controller.DialogController
import eu.kanade.tachiyomi.util.system.toast
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ConnectionsLogoutDialog(bundle: Bundle? = null) : DialogController(bundle) {

    private val service = Injekt.get<ConnectionsManager>().getService(args.getLong("serviceId"))!!

    constructor(service: ConnectionsService) : this(bundleOf("serviceId" to service.id))

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val serviceName = activity!!.getString(service.nameRes())
        return MaterialAlertDialogBuilder(activity!!)
            .setTitle(activity!!.getString(R.string.logout_title, serviceName))
            .setPositiveButton(R.string.logout) { _, _ ->
                service.logout()
                activity?.toast(R.string.logout_success)
                activity?.onBackPressed()
                (targetController as? Listener)?.connectionsLogoutDialogClosed(service)
                activity?.onBackPressed()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun connectionsLogoutDialogClosed(service: ConnectionsService)
    }
}
