package app.template.patches.nzb360

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NZB360_COMPATIBILITY

/**
 * Unlock nzb360 All Access (v23.4)
 *
 * Forces [isAASubscriptionActive] and [isUnlocked] to return true,
 * and both [isLocked] overloads to return false, bypassing all
 * per-service module paywalls (SABnzbd, Torrents, Radarr, Sonarr, etc.).
 */
@Suppress("unused")
val nzb360UnlockAllAccessPatch = bytecodePatch(
    name = "Unlock Lifetime Access",
    description = "Unlocks Lifetime access in Nzb360.",
    default = true
) {
    compatibleWith(NZB360_COMPATIBILITY)

    execute {

        fun forceBoolean(value: Boolean, vararg fps: Fingerprint) {
            val body = if (value) "const/4 v0, 0x1\nreturn v0" else "const/4 v0, 0x0\nreturn v0"
            fps.forEach { fp ->
                runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, body)
                }
            }
        }

        forceBoolean(true, IsAASubscriptionActiveFingerprint, IsUnlockedFingerprint)
        forceBoolean(false, IsLockedTwoArgFingerprint, IsLockedOneArgFingerprint)
    }
}
