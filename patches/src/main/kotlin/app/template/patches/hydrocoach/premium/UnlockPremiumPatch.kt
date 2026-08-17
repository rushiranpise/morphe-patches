package app.template.patches.hydrocoach.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HYDROCOACH_COMPATIBILITY
import app.template.patches.shared.clearBody

// ─────────────────────────────────────────────────────────────────────────────
// Hydro Coach Premium + Ad Removal — three layers on a83 (UserState)
//
// All three targets are PUBLIC STATIC methods in the same R8-obfuscated class
// (a83), located via classFingerprint on stable Kotlin @JvmStatic method names.
//
// Layer 1 — getHasProFeatures(a83)Z → true
//   The single master gate. Returns true if:
//     getPurchasedPro()                    — any pro SKU in purchases map
//     getPurchasedUpgradeNoAdsToPro()      — no-ads→pro upgrade
//     hasUnlockedPurchaseFromPromo(jp.B/C) — promo unlock
//     isSubscribedToPro()                  — RC subscription
//   Returning true from here cascades through all feature gates:
//     getHasAllThemes()  → delegates to getHasProFeatures
//     getShowAds()       → checks getHasProFeatures first (no ads if pro)
//     All composable/fragment isPro gates throughout the app
//
// Layer 2 — getShowAds(a83)Z → false
//   Belt+suspenders for ad removal. getShowAds() checks getHasProFeatures
//   first but also has independent paths for getPurchasedNoAds and promos.
//   Forcing to false ensures ads are never shown regardless of call order.
//   .registers 3: v0=local, v1=local, p0=a83 param → no expansion needed.
//
// Layer 3 — getPurchasedNoAds(a83)Z → true
//   The no-ads-only SKU check (jp.D = "no_ads"). Called from getShowAds.
//   .registers 2: v0=local, p0=a83 param → const/4 v0 + return v0 safe.
//
// Register notes (static methods — no 'this', p0 = first explicit param):
//   All three return Z with 1 param (La83;).
//   Cleared body + const/4 v0, 0x1 + return v0 uses only v0 — safe for
//   .registers 2 (v0=local, p0=a83) and .registers 3 (v0,v1=local, p0=a83).
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all pro features and removes ads in Hydro Coach by " +
        "forcing the master premium gate, ads gate, and no-ads SKU check to " +
        "return true/false respectively.",
    default = true,
) {
    compatibleWith(HYDROCOACH_COMPATIBILITY)

    execute {

        // ── Layer 1: getHasProFeatures → true ────────────────────────────────
        //
        // Original body (.registers 3, 27 instructions):
        //   Calls getPurchasedPro, getPurchasedUpgradeNoAdsToPro,
        //   hasUnlockedPurchaseFromPromo ×2, isSubscribedToPro.
        //   Returns true if any returns true, false otherwise.
        //
        // Patched: clearBody + const/4 v0, 0x1 + return v0
        // p0 (a83 param) not needed — body is self-contained.
        HasProFeaturesFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }

        // ── Layer 2: getShowAds → false ───────────────────────────────────────
        //
        // Original body (.registers 3):
        //   if getHasProFeatures → false
        //   if getPurchasedNoAds → false
        //   if hasUnlockedPurchaseFromPromo → false
        //   else → true (show ads)
        //
        // Patched: clearBody + const/4 v0, 0x0 + return v0 (false = no ads)
        ShowAdsFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
                """.trimIndent(),
            )
        }

        // ── Layer 3: getPurchasedNoAds → true ────────────────────────────────
        //
        // Original body (.registers 2):
        //   Looks up jp.D ("no_ads") in a83.purchases HashMap.
        //   Returns true if found and valid.
        //
        // Patched: clearBody + const/4 v0, 0x1 + return v0
        PurchasedNoAdsFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }
    }
}
