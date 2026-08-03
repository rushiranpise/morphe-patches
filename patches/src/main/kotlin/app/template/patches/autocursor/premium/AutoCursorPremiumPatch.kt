package app.template.patches.autocursor.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AUTOCURSOR_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Auto Cursor Premium Patch
 *
 * Auto Cursor stores its unlock state as a single SharedPreferences boolean
 * under the key "IS_PURCHASED_PREF" (obfuscated via Base64+XOR at runtime).
 *
 * All premium gates in the app ultimately call MainPref.unlocked(), which
 * delegates to gp1.y() — the single root check. Patching y() to return true
 * propagates through AllPref, all UI activities, the cursor service, and the
 * menu activity without requiring any additional patches.
 *
 * No server-side validation is involved; the check is purely local.
 */
@Suppress("unused")
val autoCursorPremiumPatch = bytecodePatch(
    name = "Auto Cursor Premium",
    description = "Unlocks Auto Cursor Pro by bypassing the local purchase state check.",
    default = true
) {
    compatibleWith(AUTOCURSOR_COMPATIBILITY)

    execute {
        IsUnlockedFingerprint.method.returnEarly(true)
    }
}
