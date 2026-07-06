package app.template.patches.picturemushroom

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Picture Mushroom premium features.",
) {
    compatibleWith(Constants.PICTUREMUSHROOM_COMPATIBILITY)
    dependsOn(
        pictureMushroomCertSeedPatch,
        pictureMushroomMapApiKeyPatch,
        bypassNativeKeyCheckPatch,
        spoofSignatureVerificationPatch,
    )

    execute {
        listOf(
            AppViewModelIsVipFingerprint.method,
            AppViewModelIsVipInHistoryFingerprint.method,
            VipInfoIsVipFingerprint.method,
            UserGetVipFingerprint.method,
            PaymentUtilsIsPaddingVipFingerprint.method,
        ).forEach { method ->
            method.clearBody()
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
