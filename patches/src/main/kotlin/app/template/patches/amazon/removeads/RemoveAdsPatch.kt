package app.template.patches.amazon.removeads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

private const val HELPER = "Lapp/template/extension/extension/AmazonHelper;"

@Suppress("unused")
val amazonRemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Hides sponsored and ad content in Amazon Shopping via CSS injection.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        // Normal page loads: p1=WebView
        MShopWebViewClientOnPageFinishedFingerprint.method.addInstructions(
            0,
            "invoke-static {p1}, $HELPER->injectAdBlock(Landroid/webkit/WebView;)V",
        )

        // All pages when WebView assigned to fragment: p1=MShopWebView
        InteractionWebFragmentSetWebViewFingerprint.method.addInstructions(
            0,
            "invoke-static {p1}, $HELPER->injectAdBlock(Landroid/webkit/WebView;)V",
        )
    }
}
