package app.template.patches.inscodeautoclicker

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INSCODE_AUTOCLICKER_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val inscodeAutoclickerForcePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in Clickmate by bypassing the purchase check.",
    default = true,
) {
    compatibleWith(INSCODE_AUTOCLICKER_COMPATIBILITY)

    execute {
        // ZipoApps PremiumHelper: has_active_purchase getter → always return true.
        // Fingerprint anchored on string("has_active_purchase") + methodCall(SharedPreferences,
        // getBoolean) — no obfuscated class or method names used.
        HasActivePurchaseFingerprint.method.returnEarly(true)
    }
}
