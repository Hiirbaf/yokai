package eu.kanade.tachiyomi.ui.setting.connections

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.service.ConnectionsService
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.launch

class ConnectionsLoginDialog(
    private val service: ConnectionsService
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_connection_login, null)

        val usernameInput = view.findViewById<EditText>(R.id.username)
        val passwordInput = view.findViewById<EditText>(R.id.password)

        return AlertDialog.Builder(context)
            .setTitle(getString(R.string.login_title, getString(service.nameRes())))
            .setView(view)
            .setPositiveButton(R.string.login) { _, _ ->
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()
                login(context, service, username, password)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun login(context: Context, service: ConnectionsService, username: String, password: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                service.login(username, password)
                context.toast(R.string.login_success)
            } catch (e: Exception) {
                service.logout()
                context.toast(e.message ?: "Login failed")
            }
        }
    }
}
