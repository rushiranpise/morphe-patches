package app.template.patches.amazon.pricecharts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.patch.stringsOption
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
            "1 month" to "1m",
            "6 months" to "6m",
            "1 year" to "1y",
            "3 years" to "3y",
            "5 years" to "5y",
            "All time" to "all",
        ),
        title = "Chart period",
        description = "Default time range for the price history charts.",
    )
    val toggle by booleanOption(
        key = "amazonPriceChartToggle",
        default = true,
        title = "Show period toggle",
        description = "Add period buttons under each chart to switch views.",
    )
    val togglePeriods by stringsOption(
        key = "amazonPriceChartTogglePeriods",
        default = listOf("1m", "6m", "1y", "3y", "5y", "all"),
        values = mapOf(
            "1 month" to listOf("1m"),
            "6 months" to listOf("6m"),
            "1 year" to listOf("1y"),
            "3 years" to listOf("3y"),
            "5 years" to listOf("5y"),
            "All time" to listOf("all"),
        ),
        title = "Toggle periods",
        description = "Which periods appear in the toggle buttons.",
    )
    val series by stringOption(
        key = "amazonKeepaSeries",
        default = "all",
        values = mapOf(
            "New only" to "new",
            "New + Used" to "new_used",
            "New + Used + Amazon" to "all",
        ),
        title = "Keepa price lines",
        description = "Which price series the Keepa chart shows.",
    )
    val chartType by stringOption(
        key = "amazonCccChartType",
        default = "new_used",
        values = mapOf(
            "New + Used" to "new_used",
            "New only" to "new",
            "Used only" to "used",
        ),
        title = "CamelCamelCamel chart",
        description = "Which price lines the CCC chart shows.",
    )
    val hideZero by booleanOption(
        key = "amazonCccHideZero",
        default = false,
        title = "Hide zero-price gaps",
        description = "Omit flat zero-price segments on the CCC chart.",
    )
    val size by stringOption(
        key = "amazonChartSize",
        default = "625",
        values = mapOf(
            "Compact" to "500",
            "Default" to "625",
            "Large" to "750",
        ),
        title = "Chart size",
        description = "Width of the CCC chart image (Keepa renders at a fixed size).",
    )
    val collapsed by booleanOption(
        key = "amazonChartsCollapsed",
        default = true,
        title = "Start collapsed",
        description = "Render the charts inside a collapsed section; tap to expand.",
    )

    execute {
        // Pack every setting into one pipe-separated string so both injection
        // sites only need a single extra register (onFragmentPostShown is full
        // at .locals 4 with v0-v3). Order: period|toggle|periods|series|type|zero|width|collapsed
        val config = listOf(
            period ?: "1y",
            if (toggle ?: true) "1" else "0",
            (togglePeriods ?: listOf("1m", "6m", "1y", "3y", "5y", "all")).joinToString(","),
            series ?: "all",
            chartType ?: "new_used",
            if (hideZero ?: false) "1" else "0",
            size ?: "625",
            if (collapsed ?: true) "1" else "0",
        ).joinToString("|")
            // Option values are constrained to this charset before they reach a
            // smali string; the extension additionally validates each field and
            // JSON-encodes it before any JavaScript is generated.
            .filter { it.isLetterOrDigit() || it == ',' || it == '|' || it == '_' }

        // Non-jumpstarted: p1=WebView, p2=url (method has 10 registers, v0 free)
        MShopWebViewClientOnPageFinishedFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$config"
                invoke-static {p1, p2, v0}, $HELPER->injectPriceCharts(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;)V
            """.trimIndent(),
        )

        // Jumpstarted + all: .locals 4, get mWebView + url via getNavRequestUrl
        InteractionWebFragmentPostShownFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/amazon/mobile/mash/MASHWebFragment;->mWebView:Lcom/amazon/mobile/mash/MASHWebView;
                invoke-virtual {p0}, Lcom/amazon/mobile/mash/MASHWebFragment;->getNavRequestUrl()Ljava/lang/String;
                move-result-object v1
                const-string v2, "$config"
                invoke-static {v0, v1, v2}, $HELPER->injectPriceCharts(Landroid/webkit/WebView;Ljava/lang/String;Ljava/lang/String;)V
            """.trimIndent(),
        )
    }
}
