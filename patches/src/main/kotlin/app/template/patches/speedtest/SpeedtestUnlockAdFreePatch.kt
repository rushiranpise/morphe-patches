package app.template.patches.speedtest

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SPEEDTEST_COMPATIBILITY

/**
 * Unlock Speedtest by Ookla Ad-Free (v7.0.4)
 *
 * The app gates ad display behind three independent boolean checks:
 *
 * 1. GooglePurchaseManager.b()Z — runtime ad-free query in the live
 *    purchase/billing pipeline. Patching to true removes ads on every
 *    speed-test result screen and home screen.
 *
 * 2. PurchaseDataCompat.b(k0)Z (isUserAdFree) — static utility called
 *    from multiple UI entry points to decide ad visibility. Covers the
 *    case where D.b() delegates here.
 *
 * 3. PurchaseDataCompat.c(k0)Z (hasInAppTokens) — checks that at least
 *    one active in-app purchase token exists. Forcing true prevents the
 *    "no purchase" fallback which re-enables ads.
 *
 * 4. SharedPrefsAdFreeCache.b()Z — legacy SharedPreferences cache read
 *    (purchase/feature.ad_free key). Forcing true keeps the cached state
 *    as ad-free across cold starts and process kills.
 */
@Suppress("unused")
val speedtestUnlockAdFreePatch = bytecodePatch(
    name = "Unlock Ad-Free",
    description = "Removes ads and unlocks ad-free status in Speedtest by Ookla.",
    default = true
) {
    compatibleWith(SPEEDTEST_COMPATIBILITY)

    execute {
        fun forceTrue(vararg fps: app.morphe.patcher.Fingerprint) {
            fps.forEach { fp ->
                runCatching { fp.match(classDefBy(fp.definingClass!!)).method }
                    .getOrNull()
                    ?.apply {
                        if (implementation == null) return@apply
                        removeInstructions(0, instructions.count())
                        addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    }
            }
        }

        forceTrue(
            PurchaseManagerIsAdFreeFingerprint,
            PurchaseDataCompatIsUserAdFreeFingerprint,
            PurchaseDataCompatHasInAppTokensFingerprint,
            SharedPrefsIsAdFreeFingerprint
        )
    }
}
