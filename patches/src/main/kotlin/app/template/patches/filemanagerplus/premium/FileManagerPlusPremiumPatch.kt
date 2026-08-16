package app.template.patches.filemanagerplus.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FILE_MANAGER_PLUS_COMPATIBILITY
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

/**
 * Unlocks File Manager - Files, PDF, Zip premium by bypassing the custom
 * RSA-signed license token verification system.
 *
 * ## License Architecture
 *
 * The app uses a custom in-house license SDK (`ax.k3.c`) that:
 * 1. Stores RSA-signed license tokens in SharedPreferences under
 *    `"license_token<productCategory>"` keys (e.g. `"license_tokenpremium_basic"`)
 * 2. On startup calls `K()` to load + verify tokens via RSA public key from
 *    raw resource and binds them to the device ID (MANUFACTURER:MODEL:AndroidID)
 * 3. UI queries `F(category)Z` → `z(category)` (token lookup) → `y(lb.b)`
 *    (state/expiry evaluation) → returns true only if ACTIVE_SUBSCRIPTION or
 *    ACTIVE_ONETIME enum value returned
 * 4. `H(category)Z` wraps `F()` with additional logging
 * 5. `l(category)Z` checks SUBSCRIPTION type specifically
 *
 * Server-side component: `https://file-manager-plus-65d18.appspot.com/`
 * issues the RSA-signed tokens. The tokens are bound to the device ID, so
 * server-side token generation cannot be bypassed without the server.
 *
 * ## Patch Strategy
 *
 * Patch all three boolean gate methods to always return true:
 * - `F(String)Z` — primary isPremium check (all feature gates)
 * - `H(String)Z` — hasPremium with debug logging (UI display)
 * - `l(String)Z` — isSubscriptionActive check
 *
 * This avoids any interaction with the RSA/token/device-ID system entirely.
 */
@Suppress("unused")
val fileManagerPlusPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all File Manager premium features by bypassing the RSA license token verification.",
) {
    compatibleWith(FILE_MANAGER_PLUS_COMPATIBILITY)

    dependsOn(
        spoofInstallSourcePatch,
        spoofSignatureVerificationPatch,
    )

    execute {
        IsPremiumFingerprint.method.returnEarly(true)
        HasPremiumWithLogFingerprint.method.returnEarly(true)
        IsSubscriptionActiveFingerprint.method.returnEarly(true)
    }
}
