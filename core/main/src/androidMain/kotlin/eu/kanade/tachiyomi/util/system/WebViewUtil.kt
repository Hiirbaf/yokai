package eu.kanade.tachiyomi.util.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import co.touchlab.kermit.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object WebViewUtil {
    const val MINIMUM_WEBVIEW_VERSION = 118

    fun supportsWebView(context: Context): Boolean {
        try {
            // May throw android.webkit.WebViewFactory$MissingWebViewPackageException if WebView
            // is not installed
            CookieManager.getInstance()
        } catch (e: Throwable) {
            Logger.e(e) { "Unable to manage cookie for WebView" }
            return false
        }

        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
    }

    /**
     * Devuelve un User-Agent modificado para parecerse al de Chrome móvil.
     */
    fun getInferredUserAgent(context: Context): String {
        return WebView(context)
            .getDefaultUserAgentString()
            .replace("; Android .*?\\)".toRegex(), "; Android 10; K)")
            .replace("Version/.* Chrome/".toRegex(), "Chrome/")
    }
}

fun WebView.isOutdated(): Boolean {
    return getWebViewMajorVersion() < WebViewUtil.MINIMUM_WEBVIEW_VERSION
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupLikeSY(
    context: Context,
    url: String,
    referer: String? = null
) {
    // Configuración básica
    with(settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
        builtInZoomControls = true
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_DEFAULT

        // UA idéntico a TachiyomiSY
        userAgentString =
            "Mozilla/5.0 (Linux; Android 10; K) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/135.0.0.0 Mobile Safari/537.36"
    }

    // Cookies de terceros
    CookieManager.getInstance().acceptThirdPartyCookies(this)

    // WebViewClient para manejar redirecciones dentro del WebView
    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?) = false
    }

    // Headers extra opcionales
    val extraHeaders = mutableMapOf<String, String>()
    referer?.let { extraHeaders["Referer"] = it }

    // Cargar URL con headers
    loadUrl(url, extraHeaders)

    // Log debug
    co.touchlab.kermit.Logger.i { "WebView cargando $url con UA: ${settings.userAgentString}" }
}

private fun WebView.getWebViewMajorVersion(): Int {
    val uaRegexMatch = """.*Chrome/(\d+)\..*""".toRegex().matchEntire(getDefaultUserAgentString())
    return if (uaRegexMatch != null && uaRegexMatch.groupValues.size > 1) {
        uaRegexMatch.groupValues[1].toInt()
    } else {
        0
    }
}

// Basado en https://stackoverflow.com/a/29218966
private fun WebView.getDefaultUserAgentString(): String {
    val originalUA: String = settings.userAgentString

    // Next call to getUserAgentString() will get us the default
    settings.userAgentString = null
    val defaultUserAgentString = settings.userAgentString

    // Revertir al original
    settings.userAgentString = originalUA

    return defaultUserAgentString
}

suspend fun WebView.getHtml(): String = suspendCancellableCoroutine {
    evaluateJavascript("document.documentElement.outerHTML") { html -> it.resume(html) }
}
