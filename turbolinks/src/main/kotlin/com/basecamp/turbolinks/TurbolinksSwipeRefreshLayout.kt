package com.basecamp.turbolinks

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import androidx.core.view.children
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

internal class TurbolinksSwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        SwipeRefreshLayout(context, attrs) {

    override fun canChildScrollUp(): Boolean {
        val webView = children.firstOrNull() as? WebView
        return webView?.scrollY ?: 0 > 0
    }
}
