package app.template.patches.protonmail.composer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PROTONMAIL_COMPATIBILITY
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

/**
 * Unlocks the custom date/time picker in scheduled send.
 *
 * .registers 1 means only p0 exists (no v0). Use const/4 p0 + return p0.
 * returnEarly(true) would inject const/4 v0 which doesn't exist → VerifyError.
 */
@Suppress("unused")
val protonMailCustomTimePatch = bytecodePatch(
    name = "Unlock Custom Time Picker",
    description = "Unlocks the custom scheduled send date/time picker for all users.",
) {
    compatibleWith(PROTONMAIL_COMPATIBILITY)

    dependsOn(
        spoofInstallSourcePatch,
        spoofSignatureVerificationPatch,
        spoofFirebaseCertHashPatch,
    )

    execute {
        IsCustomTimeSendOptionAvailableFingerprint.method.addInstructions(
            0,
            "const/4 p0, 0x1\nreturn p0",
        )
    }
}
