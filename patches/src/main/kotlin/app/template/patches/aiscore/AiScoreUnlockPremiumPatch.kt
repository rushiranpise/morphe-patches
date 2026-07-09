package app.template.patches.aiscore

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AISCORE_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val aiScoreUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks AiScore Premium Features in app.",
    default = true,
) {
    compatibleWith(AISCORE_COMPATIBILITY)

    execute {
        listOf(
            UserPreferenceIsLoggedInFingerprint,
            UserPreferenceIsVipFingerprint,
            UserPreferenceVipFlagFingerprint,
            UserProtoIsVipFingerprint,
            TipsDetailIsFreeFingerprint,
            TipsDetailShowPaidContentFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        listOf(
            UserPreferenceVipExpiryFingerprint,
            UserProtoVipExpiryFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(0, "const-wide v0, 0xf4865700L\nreturn-wide v0")
            }
        }

        TipsDetailPurchaseTimeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const v0, 0x5e0be100\nreturn v0")
        }
    }
}
