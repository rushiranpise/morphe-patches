package app.template.patches.inscodeautoclicker

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INSCODE_AUTOCLICKER_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Inscode AutoClicker (Clickmate) v7.1.4 — Unlock Premium patch
//
// CHANGELOG FROM previous version:
//   Old: replaceInstruction(lastIndex - 1, "const/4 v0, 0x1")
//        Fragile — lastIndex arithmetic breaks if ZipoApps SDK adds instructions.
//   New: returnEarly(true) at index 0 — injects "const/4 v0, 0x1 / return v0"
//        before any existing instructions. Robust regardless of method body changes.
//
// TARGET: com/zipoapps/premiumhelper/d.x()Z
//   Reads "has_active_purchase" boolean from the SDK's SharedPreferences file
//   ("premium_helper_data") and returns it. This is the single gate the SDK
//   uses throughout the app to decide whether premium features are unlocked.
//   Forcing it to always return true unlocks all premium functionality.

@Suppress("unused")
val inscodeAutoclickerForcePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in Clickmate by bypassing the purchase check.",
    default = true,
) {
    compatibleWith(INSCODE_AUTOCLICKER_COMPATIBILITY)

    execute {
        HasActivePurchaseFingerprint.method.returnEarly(true)
    }
}
