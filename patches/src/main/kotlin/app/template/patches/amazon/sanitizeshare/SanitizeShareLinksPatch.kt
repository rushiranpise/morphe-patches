package app.template.patches.amazon.sanitizeshare

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

private const val HELPER = "Lapp/template/extension/extension/AmazonHelper;"

@Suppress("unused")
val amazonSanitizeShareLinksPatch = bytecodePatch(
    name = "Sanitize share links",
    description = "Strips tracking parameters from copied/shared Amazon links, leaving only the clean product URL.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        // p1=url String, .locals 2 - replace p1 with sanitized version before copy
        ShareToClipboardActivityFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p1}, $HELPER->sanitizeAmazonUrl(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
            """.trimIndent(),
        )
    }
}
