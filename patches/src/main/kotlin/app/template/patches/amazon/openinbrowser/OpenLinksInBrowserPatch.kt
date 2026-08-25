package app.template.patches.amazon.openinbrowser

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

private const val HELPER = "Lapp/template/extension/extension/AmazonHelper;"

@Suppress("unused")
val amazonOpenLinksInBrowserPatch = bytecodePatch(
    name = "Open links in browser",
    description = "Opens non-Amazon URLs in the default browser instead of the in-app WebView.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        // p1=WebView, p2=url (.locals 3)
        // The helper decides using the parsed hostname, not a substring match, so
        // hosts that merely contain "amazon." (evilamazon.com,
        // amazon.com.evil.example, amazon.com@evil.example) leave the WebView.
        // It returns true once the destination has been handed to the browser.
        MShopWebViewClientShouldOverrideFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p1, p2}, $HELPER->openExternally(Landroid/webkit/WebView;Ljava/lang/String;)Z
                move-result v0
                if-eqz v0, :skip
                const/4 v0, 0x1
                return v0
                :skip
                nop
            """.trimIndent(),
        )
    }
}
