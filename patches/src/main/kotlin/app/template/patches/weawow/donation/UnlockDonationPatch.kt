package app.template.patches.weawow.donation

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WEAWOW_COMPATIBILITY

private val DonationStatusFingerprint = Fingerprint(
    definingClass = "LI2/m;",
    name = "h0",
    returnType = "Ljava/lang/String;",
    strings = listOf("donated"),
)

@Suppress("unused")
val unlockDonationPatch = bytecodePatch(
    name = "Unlock donation",
    description = "Forces h0() to return \"yes\" so donate dialog never shows and providers unlock.",
) {
    compatibleWith(WEAWOW_COMPATIBILITY)

    execute {
        DonationStatusFingerprint
            .match(classDefBy(DonationStatusFingerprint.definingClass!!))
            .method
            .addInstructions(0, "const-string v0, \"yes\"\nreturn-object v0")
    }
}
