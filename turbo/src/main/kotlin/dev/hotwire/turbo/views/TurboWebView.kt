package dev.hotwire.turbo.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.webkit.WebViewCompat
import dev.hotwire.turbo.util.contentFromAsset
import dev.hotwire.turbo.util.runOnUiThread
import dev.hotwire.turbo.util.toJson
import dev.hotwire.turbo.visit.TurboVisitOptions
import com.google.gson.GsonBuilder
import dev.hotwire.turbo.util.logEvent

/**
 * A Turbo-specific WebView that configures required settings and exposes some helpful info.
 *
 * Generally, you are not creating this view manually — it will be automatically created
 * and available from the Turbo session.
 */
@SuppressLint("SetJavaScriptEnabled")
open class TurboWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    WebView(context, attrs) {
    private val gson = GsonBuilder().disableHtmlEscaping().create()

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    /**
     * Provides the WebView's package name (corresponds to Chrome or Android System WebView).
     */
    val packageName: String?
        get() = WebViewCompat.getCurrentWebViewPackage(context)?.packageName

    /**
     * Provides the WebView's version name (corresponds to Chrome or Android System WebView).
     */
    val versionName: String?
        get() = WebViewCompat.getCurrentWebViewPackage(context)?.versionName

    /**
     * Provides the WebView's major version (corresponds to Chrome or Android System WebView).
     */
    val majorVersion: Int?
        get() = versionName?.substringBefore(".")?.toIntOrNull()

    internal fun visitLocation(location: String, options: TurboVisitOptions, restorationIdentifier: String) {
        val args = encodeArguments(location, options.toJson(), restorationIdentifier)

        installBridge(onBridgeInstalled = {
            logEvent("ReInstallBridge", listOf("method" to "visitLocation"))
            runJavascript("turboNative.visitLocationWithOptionsAndRestorationIdentifier($args)")
        }, onBridgeComplete = {
            runJavascript("turboNative.visitLocationWithOptionsAndRestorationIdentifier($args)")
        })
    }

    internal fun visitRenderedForColdBoot(coldBootVisitIdentifier: String) {
        installBridge(onBridgeInstalled = {
            logEvent("ReInstallBridge", listOf("method" to "visitRenderedForColdBoot"))
            runJavascript("turboNative.visitRenderedForColdBoot('$coldBootVisitIdentifier')")
        }, onBridgeComplete = {
            runJavascript("turboNative.visitRenderedForColdBoot('$coldBootVisitIdentifier')")
        })
    }

    internal fun installBridge(onBridgeComplete: () -> Unit = {}, onBridgeInstalled: () -> Unit) {
        val script = "window.turboNative == null"
        val bridge = context.contentFromAsset("js/turbo_bridge.js")

        runJavascript(script) { s ->
            if (s?.toBoolean() == true) {
                runJavascript(bridge) {
                    onBridgeInstalled()
                }
            } else {
                onBridgeComplete()
            }
        }
    }

    private fun WebView.runJavascript(javascript: String, onComplete: (String?) -> Unit = {}) {
        context.runOnUiThread {
            evaluateJavascript(javascript) {
                onComplete(it)
            }
        }
    }

    private fun encodeArguments(vararg args: Any): String? {
        return args.joinToString(",") { gson.toJson(it) }
    }
}
