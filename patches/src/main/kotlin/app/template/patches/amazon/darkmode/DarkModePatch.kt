package app.template.patches.amazon.darkmode

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY
import org.w3c.dom.Element

private const val HELPER = "Lapp/template/extension/extension/AmazonHelper;"

private val amazonDarkModeResourcePatch = resourcePatch {
    execute {
        // v32.12 and below: forceDarkAllowed lives in res/values-v29/styles.xml
        // v32.13+: values-v29 removed, attribute moved to res/values/styles.xml
        // Try both so the patch works across versions without an ENOENT crash.
        val candidates = listOf("res/values-v29/styles.xml", "res/values/styles.xml")
        var patched = 0
        for (path in candidates) {
            runCatching {
                document(path).use { doc ->
                    val items = doc.getElementsByTagName("item")
                    for (i in 0 until items.length) {
                        val item = items.item(i) as? Element ?: continue
                        if (item.getAttribute("name") == "android:forceDarkAllowed") {
                            item.textContent = "true"
                            patched++
                        }
                    }
                }
            }
        }
        if (patched == 0) throw Exception(
            "android:forceDarkAllowed not found in any styles.xml — " +
                "check resource layout for this version",
        )
    }
}

@Suppress("unused")
val amazonDarkModePatch = bytecodePatch(
    name = "Dark mode",
    description = "Force dark mode for Amazon Shopping.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)
    dependsOn(amazonDarkModeResourcePatch)
    extendWith("extensions/extension.mpe")

    val mode by stringOption(
        key = "amazonDarkMode",
        default = "follow_system",
        values = mapOf(
            "Off" to "off",
            "Follow system" to "follow_system",
            "Always on" to "on",
        ),
        title = "Dark mode",
        description = "Off / Follow system / Always on.",
    )

    execute {
        val m = mode ?: "follow_system"
        if (m == "off") return@execute

        // WebView CSS fixes: .locals 1, mWebView=v0, p1=url
        MShopWebViewContainerOnPageFinishedFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/amazon/mShop/web/MShopWebViewContainer;->mWebView:Lcom/amazon/mShop/web/MShopWebView;
                const-string p1, "$m"
                invoke-static {v0, p1}, $HELPER->injectDarkMode(Landroid/webkit/WebView;Ljava/lang/String;)V
            """.trimIndent(),
        )

        // Tab icon tint: .locals 2, v0 available
        BaseTabControllerGetTabIconFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "$m"
                invoke-static {v0}, $HELPER->tintTabIconIfDark(Ljava/lang/String;)V
            """.trimIndent(),
        )
    }
}
