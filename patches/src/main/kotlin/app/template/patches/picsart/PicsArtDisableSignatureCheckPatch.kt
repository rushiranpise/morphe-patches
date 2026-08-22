package app.template.patches.picsart

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import app.template.patches.shared.clearBody

/**
 * Disables PicsArt's anti-tamper signature check.
 *
 * `SignatureCheckInit` (an appstart item) compares the MD5 of the app's signing
 * certificate against values embedded in an asset, and on mismatch schedules
 * `System.exit(-1)` after a random 8–23s delay on a background thread. Any
 * re-signed build (which is required for patching) fails this check, so the app
 * boots, shows UI for ~12s, then dies silently.
 *
 * No-op'ing `initialize(Context)V` means the exit is never scheduled.
 */

internal val picsartDisableSignatureCheckPatch = bytecodePatch{
    dependsOn(spoofInstallSourcePatch, spoofSignatureVerificationPatch)

    execute {
        val method = SignatureCheckInitFingerprint.method
        method.clearBody()
        method.addInstructions(
            0,
            """
                return-void
            """.trimIndent(),
        )
    }
}
