package app.template.patches.netshare

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NETSHARE_COMPATIBILITY

@Suppress("unused")
val netShareUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Pro features in app.",
    default = true
) {
    compatibleWith(NETSHARE_COMPATIBILITY)

    execute {
        // ── Layer 1: Util.isPro(Context)Z → always true ───────────────────────
        // Checks if "netshare.key" companion app is installed via com.android.vending.
        runCatching {
            UtilIsProFingerprint
                .match(classDefBy(UtilIsProFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )

        // ── Layer 2: IAB.isPurchased(String, Context)Z → always true ──────────
        // Checks reward timer + SharedPref keys "any"/"all" + calls Util.isPro().
        runCatching {
            IABIsPurchasedFingerprint
                .match(classDefBy(IABIsPurchasedFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )
    }
}
