package app.template.patches.amazon.videautoplay

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

private const val HELPER = "Lapp/template/extension/extension/AmazonHelper;"

@Suppress("unused")
val amazonDisableVideoAutoplayPatch = bytecodePatch(
    name = "Disable video autoplay",
    description = "Prevents product and ad videos from autoplaying.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        // .locals 4 - v0/v1 safe; get mWebView
        InteractionWebFragmentPostShownFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/amazon/mobile/mash/MASHWebFragment;->mWebView:Lcom/amazon/mobile/mash/MASHWebView;
                invoke-static {v0}, $HELPER->injectDisableVideoAutoplay(Landroid/webkit/WebView;)V
            """.trimIndent(),
        )
    }
}
