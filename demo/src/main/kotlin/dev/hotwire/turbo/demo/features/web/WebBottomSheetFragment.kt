package dev.hotwire.turbo.demo.features.web

import android.webkit.WebResourceRequest
import dev.hotwire.turbo.demo.base.NavDestination
import dev.hotwire.turbo.fragments.TurboWebBottomSheetDialogFragment
import dev.hotwire.turbo.nav.TurboNavGraphDestination

@TurboNavGraphDestination(uri = "turbo://fragment/web/modal/sheet")
class WebBottomSheetFragment : TurboWebBottomSheetDialogFragment(), NavDestination {
    override fun createWebResourceRequest(): WebResourceRequest? {
        return null
    }
}
