package app.template.patches.teams.privacy

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TEAMS_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.teams.integrity.teamsIntegrityBypassDependency

@Suppress("unused")
val teamsPrivacyPatch = bytecodePatch(
    name = "Privacy Enhancements",
    description = "Suppresses outbound read receipts and prevents admin quiet-hours " +
        "policies from overriding your presence status.",
    default = true,
) {
    compatibleWith(TEAMS_COMPATIBILITY)
    dependsOn(teamsIntegrityBypassDependency())

    execute {
        // Disable outbound read receipts — your messages show as "Sent", never "Read".
        readReceiptsFingerprint.method.returnEarly(false)

        // Prevent quiet-hours / shift-end policies from forcing presence to Away.
        blockingModeFingerprint.method.returnEarly(false)
    }
}

@JvmSynthetic
internal fun teamsPrivacyDependency() = teamsPrivacyPatch
