package app.template.patches.amazon.disableautoplay

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

/**
 * Ported from amznkiller v2.5.0 (feat: disable video autoplay in webviews).
 * Original uses Xposed to hook onPageFinished and inject JS; we use a bytecode
 * patch on MShopWebView.initialize() to call setMediaPlaybackRequiresUserGesture(true)
 * at WebView creation time — cleaner and fires before any page loads.
 *
 * Registers in initialize(): .registers 3 → p0=this, v0 and v1 available.
 * We call p0.getSettings() → v0, then v0.setMediaPlaybackRequiresUserGesture(true).
 */
@Suppress("unused")
val amazonDisableVideoAutoplayPatch = bytecodePatch(
    name = "Disable video autoplay",
    description = "Prevents videos from autoplaying in Amazon's in-app WebView pages.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)

    execute {
        MShopWebViewInitializeFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroid/webkit/WebView;->getSettings()Landroid/webkit/WebSettings;
                move-result-object v0
                const/4 v1, 0x1
                invoke-virtual {v0, v1}, Landroid/webkit/WebSettings;->setMediaPlaybackRequiresUserGesture(Z)V
            """.trimIndent(),
        )
    }
}
