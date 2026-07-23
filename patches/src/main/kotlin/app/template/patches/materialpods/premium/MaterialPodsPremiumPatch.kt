package app.template.patches.materialpods.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MATERIAL_PODS_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Unlocks all pro features by making the master premium gate always return true.
// o5/a.c()Z reads SharedPreferences.getBoolean("PRO_MODE_ENABLED", false).
// Returning true unconditionally makes i5/d (feature/menu list builder) treat
// every feature as purchased, propagating through MenuItem.setPro() and
// ChooseVariant.isPro() to unlock all locked settings and customisation options.
@Suppress("unused")
val materialPodsPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all pro features.",
    default = true,
) {
    compatibleWith(MATERIAL_PODS_COMPATIBILITY)

    execute {
        ProModeEnabledFingerprint.method.returnEarly(true)
    }
}
