package app.template.patches.athena.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ATHENA_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Athena (com.kin.athena) stores all Settings fields in DataStore by reflection
// using each field's name as the preference key, including "premiumUnlocked".
// Premium is activated via a server-side license key check (VerifyLicenseUseCase)
// that POSTs to a remote endpoint; on success it calls Settings.copy$default with
// premiumUnlocked=true and persists the updated Settings object back to DataStore.
//
// Patch: force Settings.getPremiumUnlocked() to always return true.
// Every feature gate reads premium state through this getter on the live StateFlow
// value — no separate isActive() or subscription object is involved.
//
// Note: DataStore will still persist premiumUnlocked=false until a genuine
// license activation occurs, but because all reads go through getPremiumUnlocked()
// this patch short-circuits every check before the stored value is consulted.
@Suppress("unused")
val athenaUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Athena by making Settings.getPremiumUnlocked() always return true.",
) {
    compatibleWith(ATHENA_COMPATIBILITY)

    execute {
        GetPremiumUnlockedFingerprint.method.returnEarly(true)
    }
}
