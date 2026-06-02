package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Source: smali_classes8/com/particlemedia/feature/subscription/RadarMapSubscriptionGateway.smali
//
// When subscription enforcement is active (LE2/s.g() == true):
//   freeLimit()I    → returns 1     (only 1 item allowed for free users)
//   premiumLimit()I → returns 10    (10 item cap even for premium users)
//
// When enforcement is off (LE2/s.g() == false):
//   both return 0x7fffffff (Integer.MAX_VALUE = effectively unlimited)
//
// These limits apply to saved locations, map pins, and similar counted resources.
// Patching both methods to unconditionally return Integer.MAX_VALUE removes all
// artificial caps regardless of the feature-flag state.
//
// Source: smali_classes8/com/particlemedia/feature/subscription/PremiumEntitlementHelper.smali
//
// maxSavedLocationCount()I — a SEPARATE limit in PremiumEntitlementHelper:
//   if isPremiumActive() → return 10    ← hardcoded cap even for premium!
//   else                 → return 1
//
// This is independent of freeLimit/premiumLimit in RadarMapSubscriptionGateway.
// UnlockPremiumPatch makes isPremiumActive() return true, so users land on
// the premium branch (10) rather than the free branch (1). But 10 is still
// an artificial cap. We force this method to return Integer.MAX_VALUE as well.
//
// Smali body for freeLimit (before patch):
//   invoke-static {}, LE2/s;->g()Z
//   move-result v0
//   if-eqz v0, :cond_0
//   const/4 v0, 0x1
//   return v0
//   :cond_0
//   const v0, 0x7fffffff
//   return v0
//
// Smali body for maxSavedLocationCount (before patch):
//   invoke-static {}, Lcom/.../PremiumEntitlementHelper;->isPremiumActive()Z
//   move-result v0
//   if-eqz v0, :cond_0
//   const/16 v0, 0xa       ← 10
//   return v0
//   :cond_0
//   const/4 v0, 0x1
//   return v0
//
// After patch: all three methods unconditionally return 0x7fffffff.

private const val GATEWAY_CLASS =
    "Lcom/particlemedia/feature/subscription/RadarMapSubscriptionGateway;"

private const val ENTITLEMENT_HELPER_CLASS =
    "Lcom/particlemedia/feature/subscription/PremiumEntitlementHelper;"

object FreeLimitFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "freeLimit"
)

object PremiumLimitFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "premiumLimit"
)

object MaxSavedLocationCountFingerprint : Fingerprint(
    definingClass = ENTITLEMENT_HELPER_CLASS,
    name = "maxSavedLocationCount"
)

@Suppress("unused")
val removeLimitsPatch = bytecodePatch(
    name = "Remove Item Limits",
    description = "Removes all  item limits in-app."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        // Integer.MAX_VALUE = 0x7fffffff — same value the app returns in uncapped mode
        val maxValueInstructions = """
            const v0, 0x7fffffff
            return v0
        """.trimIndent()

        // Patch RadarMapSubscriptionGateway: freeLimit + premiumLimit
        for (fingerprint in listOf(FreeLimitFingerprint, PremiumLimitFingerprint)) {
            val method = fingerprint.match(classDefBy(GATEWAY_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, maxValueInstructions)
        }

        // Patch PremiumEntitlementHelper: maxSavedLocationCount
        val maxSavedMethod = MaxSavedLocationCountFingerprint
            .match(classDefBy(ENTITLEMENT_HELPER_CLASS))
            .method
        maxSavedMethod.removeInstructions(0, maxSavedMethod.instructions.count())
        maxSavedMethod.addInstructions(0, maxValueInstructions)
    }
}