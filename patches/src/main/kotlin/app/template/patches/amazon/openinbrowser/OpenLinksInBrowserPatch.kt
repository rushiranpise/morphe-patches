package app.template.patches.amazon.openinbrowser

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

@Suppress("unused")
val amazonOpenLinksInBrowserPatch = bytecodePatch(
    name = "Open links in browser",
    description = "Opens non-Amazon URLs in the default browser instead of the in-app WebView.",
    default = false,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY)

    execute {
        // p1=WebView, p2=url (.locals 3)
        // If url doesn't contain "amazon." fire ACTION_VIEW and return true
        MShopWebViewClientShouldOverrideFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "amazon."
                invoke-virtual {p2, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                move-result v0
                if-nez v0, :skip
                new-instance v0, Landroid/content/Intent;
                const-string v1, "android.intent.action.VIEW"
                invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V
                invoke-static {p2}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
                move-result-object v1
                invoke-virtual {v0, v1}, Landroid/content/Intent;->setData(Landroid/net/Uri;)Landroid/content/Intent;
                invoke-virtual {p1}, Landroid/webkit/WebView;->getContext()Landroid/content/Context;
                move-result-object v1
                invoke-virtual {v1, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
                const/4 v0, 0x1
                return v0
                :skip
                nop
            """.trimIndent(),
        )
    }
}
