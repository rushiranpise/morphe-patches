package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Source: smali_classes8/com/particlemedia/feature/subscription/RadarMapSubscriptionGateway.smali
//
// isFollowedLocationsPremiumEnabled()Z is gated on LE2/s.g() — a remote feature flag
// that is completely independent of isActiveNow() / SubscriptionState.isActive().
// Without this patch the Followed Locations feature remains locked even when
// BypassPaywallPatch and UnlockPremiumPatch are both applied.
//
// Smali body (before patch):
//   invoke-static {}, LE2/s;->g()Z
//   move-result v0
//   return v0
//
// After patch: unconditionally return true.

private const val GATEWAY_CLASS =
    "Lcom/particlemedia/feature/subscription/RadarMapSubscriptionGateway;"

object IsFollowedLocationsPremiumEnabledFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isFollowedLocationsPremiumEnabled"
)

@Suppress("unused")
val unlockFollowedLocationsPatch = bytecodePatch(
    name = "Unlock Followed Locations",
    description = "Unlocks the Followed Locations premium feature."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        val method = IsFollowedLocationsPremiumEnabledFingerprint
            .match(classDefBy(GATEWAY_CLASS))
            .method
        method.removeInstructions(0, method.instructions.count())
        method.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )
    }
}