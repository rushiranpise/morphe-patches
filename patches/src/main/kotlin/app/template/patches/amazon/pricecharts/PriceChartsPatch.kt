package app.template.patches.amazon.pricecharts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

private const val HELPER = "Lapp/template/extension/extension/AmazonHelper;"

@Suppress("unused")
val amazonPriceChartsPatch = bytecodePatch(
    name = "Price history charts",
    description = "Injects Keepa and CamelCamelCamel price history charts on Amazon product pages.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        // Non-jumpstarted: p1=WebView, p2=url
        MShopWebViewClientOnPageFinishedFingerprint.method.addInstructions(
            0,
            "invoke-static {p1, p2}, $HELPER->injectPriceCharts(Landroid/webkit/WebView;Ljava/lang/String;)V",
        )

        // Jumpstarted + all: .locals 4, get mWebView + url via getNavRequestUrl
        InteractionWebFragmentPostShownFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/amazon/mobile/mash/MASHWebFragment;->mWebView:Lcom/amazon/mobile/mash/MASHWebView;
                invoke-virtual {p0}, Lcom/amazon/mobile/mash/MASHWebFragment;->getNavRequestUrl()Ljava/lang/String;
                move-result-object v1
                invoke-static {v0, v1}, $HELPER->injectPriceCharts(Landroid/webkit/WebView;Ljava/lang/String;)V
            """.trimIndent(),
        )
    }
}
