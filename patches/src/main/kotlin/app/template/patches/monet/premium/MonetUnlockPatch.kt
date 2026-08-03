package app.template.patches.monet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MONET_COMPATIBILITY

// Monet (com.klevico.monet) uses Google Play Billing with a single SKU "premium_unlock".
// Premium state is managed by an obfuscated BillingManager (La/km;) which:
//   - Reads "is_premium_cached" from "billing_prefs" SharedPreferences on init
//   - Emits isPremium to a MutableStateFlow<Boolean> consumed by all UI state builders
//   - Writes "is_premium_cached" back on every billing refresh (can write false)
//
// Patch: intercept setPremiumCached(boolean) at entry and force the argument to true.
// This makes every call — whether from a billing refresh returning no purchases or from
// a successful purchase — write true into SharedPrefs and emit true into the StateFlow.
// Feature gates in e82, fs1, x32, C0540oo all read from this StateFlow path.
@Suppress("unused")
val monetUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Monet by forcing the billing cache to always report premium as active.",
) {
    compatibleWith(MONET_COMPATIBILITY)

    execute {
        // Force p1 = true at entry so the method always persists premium=true
        // and emits true to the isPremium StateFlow, regardless of actual purchase state.
        SetPremiumCachedFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )
    }
}
