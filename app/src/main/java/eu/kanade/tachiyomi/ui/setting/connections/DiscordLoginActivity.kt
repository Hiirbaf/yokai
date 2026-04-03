package eu.kanade.tachiyomi.ui.setting.connections

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import yokai.domain.connections.service.ConnectionsPreferences
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.ui.base.activity.BaseActivity
import androidx.appcompat.app.AppCompatActivity
import eu.kanade.tachiyomi.util.system.toast
import yokai.i18n.MR
import uy.kohesive.injekt.injectLazy
import java.io.File

class DiscordLoginActivity : AppCompatActivity() {

    private val connectionsPreferences: ConnectionsPreferences by injectLazy()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discord_login_activity)
        val webView = findViewById<WebView>(R.id.webview)

        webView.settings.javaScriptEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null && url.endsWith("/app")) {
                    webView.stopLoading()
                    webView.evaluateJavascript(
                        """
                        (()=>{const i=document.createElement('iframe');document.body.append(i);
                        const t=JSON.parse(i.contentWindow.localStorage.token);i.remove();return t})()
                        """.trimIndent(),
                    ) { token ->
                        val cleanToken = token.trim('"')
                        if (validateToken(cleanToken)) {
                            // Guardar el token en las preferencias
                            connectionsPreferences.connectionsToken("discord").set(cleanToken)
                            Log.d("discord_login_yokai", "Token obtenido: $cleanToken")
                            Toast.makeText(this@DiscordLoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                        } else {
                            Toast.makeText(this@DiscordLoginActivity, "No se pudo obtener token", Toast.LENGTH_SHORT).show()
                        }
                        // Limpiar cache y cerrar
                        applicationInfo.dataDir.let { File("$it/app_webview/").deleteRecursively() }
                        finish()
                    }
                }
            }
        }
        webView.loadUrl("https://discord.com/login")
    }

    private fun validateToken(token: String): Boolean =
        Regex("""^[\w-]{24}\.[\w-]{6}\.[\w-]{27}\w+?$""").matches(token)
}
