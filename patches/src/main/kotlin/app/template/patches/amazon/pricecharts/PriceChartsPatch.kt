package app.template.patches.amazon.pricecharts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
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

    val period by stringOption(
        key = "amazonPriceChartPeriod",
        default = "1y",
        values = mapOf(
            "1 year" to "1y",
            "3 years" to "3y",
            "All time" to "all",
        ),
        title = "Chart period",
        description = "Default time range for the CamelCamelCamel chart (1 year / 3 years / all time).",
    )
    val toggle by booleanOption(
        key = "amazonPriceChartToggle",
        default = true,
        title = "Show period toggle",
        description = "Add 1Y / 3Y / ALL buttons under the chart to switch views.",
    )

    execute {
        val p = period ?: "1y"
        val tg = toggle ?: true

        // Non-jumpstarted: p1=WebView, p2=url (method has 10 registers, v0/v1 free)
        MShopWebViewClientOnPageFinishedFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$p"
                const/4 v1, ${if (tg) "1" else "0"}
                invoke-static {p1, p2, v0, v1}, $HELPER->injectPriceCharts(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Z)V
            """.trimIndent(),
        )

        // Jumpstarted + all: .locals 4, get mWebView + url via getNavRequestUrl
        InteractionWebFragmentPostShownFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/amazon/mobile/mash/MASHWebFragment;->mWebView:Lcom/amazon/mobile/mash/MASHWebView;
                invoke-virtual {p0}, Lcom/amazon/mobile/mash/MASHWebFragment;->getNavRequestUrl()Ljava/lang/String;
                move-result-object v1
                const-string v2, "$p"
                const/4 v3, ${if (tg) "1" else "0"}
                invoke-static {v0, v1, v2, v3}, $HELPER->injectPriceCharts(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;Z)V
            """.trimIndent(),
        )
    }
}
