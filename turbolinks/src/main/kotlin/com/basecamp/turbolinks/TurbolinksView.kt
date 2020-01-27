package com.basecamp.turbolinks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class TurbolinksView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    private val webViewContainer: ViewGroup get() = findViewById(R.id.turbolinks_webView_container)
    private val progressContainer: ViewGroup get() = findViewById(R.id.turbolinks_progress_container)
    private val errorContainer: ViewGroup get() = findViewById(R.id.turbolinks_error_container)
    private val screenshotView: ImageView get() = findViewById(R.id.turbolinks_screenshot)

    internal val webViewRefresh: SwipeRefreshLayout? get() = webViewContainer as? SwipeRefreshLayout
    internal val errorRefresh: SwipeRefreshLayout? get() = errorContainer as? SwipeRefreshLayout

    internal fun attachWebView(webView: WebView): Boolean {
        if (webView.parent == webViewContainer) return false

        // Match the WebView background with its new parent
        if (background is ColorDrawable) {
            webView.setBackgroundColor((background as ColorDrawable).color)
        }

        webViewContainer.addView(webView)
        return true
    }

    internal fun detachWebView(webView: WebView) {
        webViewContainer.removeView(webView)
    }

    internal fun addProgressView(progressView: View) {
        // Don't show the progress view if a screenshot is available
        if (screenshotView.isVisible) return

        check(progressView.parent == null) { "Progress view cannot be attached to another parent" }

        removeProgressView()
        progressContainer.addView(progressView)
        progressContainer.isVisible = true
    }

    internal fun removeProgressView() {
        progressContainer.removeAllViews()
        progressContainer.isVisible = false
    }

    internal fun addScreenshot(screenshot: Bitmap?) {
        if (screenshot == null) return

        screenshotView.setImageBitmap(screenshot)
        screenshotView.isVisible = true
    }

    internal fun removeScreenshot() {
        screenshotView.setImageBitmap(null)
        screenshotView.isVisible = false
    }

    internal fun addErrorView(errorView: View) {
        check(errorView.parent == null) { "Error view cannot be attached to another parent" }

        errorContainer.addView(errorView)
        errorContainer.isVisible = true

        errorRefresh?.let {
            it.isEnabled = true
            it.isRefreshing = true
        }
    }

    internal fun removeErrorView() {
        errorContainer.removeAllViews()
        errorContainer.isVisible = false

        errorRefresh?.let {
            it.isEnabled = false
            it.isRefreshing = false
        }
    }

    fun createScreenshot(): Bitmap? {
        if (!isLaidOut) return null
        if (!hasEnoughMemoryForScreenshot()) return null
        if (width <= 0 || height <= 0) return null

        return drawToBitmap()
    }

    fun screenshotOrientation(): Int {
        return context.resources.configuration.orientation
    }

    private fun hasEnoughMemoryForScreenshot(): Boolean {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory().toFloat()
        val max = runtime.maxMemory().toFloat()
        val remaining = 1f - (used / max)

        return remaining > .10
    }
}
