package app.template.patches.telegram.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.IsRoundOnceFingerprint
import app.template.patches.telegram.IsSecretMediaInstanceFingerprint
import app.template.patches.telegram.IsSecretMediaStaticFingerprint
import app.template.patches.telegram.IsSecretPhotoOrVideoFingerprint
import app.template.patches.telegram.IsVoiceOnceFingerprint
import app.template.patches.telegram.MessageObjectNeedDrawBluredPreviewFingerprint
import app.template.patches.telegram.SecretMediaViewerClosePhotoFingerprint
import app.template.patches.telegram.SendSecretMediaDeleteFingerprint
import app.template.patches.telegram.SendSecretMessageReadFingerprint
import app.template.patches.telegram.ShouldEncryptPhotoOrVideoFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val telegramAntiDisappearingMediaPatch = bytecodePatch(
    name = "Anti-disappearing media",
    description = "Keeps view-once photos, videos and voice messages viewable indefinitely.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Return false — prevents the destruction timer and encrypted-only storage
        listOf(
            IsSecretMediaInstanceFingerprint,
            IsSecretMediaStaticFingerprint,
            IsSecretPhotoOrVideoFingerprint,
            ShouldEncryptPhotoOrVideoFingerprint,
            IsVoiceOnceFingerprint,
            IsRoundOnceFingerprint,
            MessageObjectNeedDrawBluredPreviewFingerprint,   // prevent blurred preview overlay
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // Return null Runnable — blocks destruction and "opened" read-receipt callbacks
        listOf(
            SendSecretMediaDeleteFingerprint,
            SendSecretMessageReadFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return-object v0
            """)
        }

        // SecretMediaViewer.closePhoto — null out the onClose field to prevent destruction
        val onCloseFieldFilter = fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lorg/telegram/ui/SecretMediaViewer;",
            name = "onClose",
        )
        Fingerprint(
            definingClass = "Lorg/telegram/ui/SecretMediaViewer;",
            name = "closePhoto",
            filters = listOf(onCloseFieldFilter),
        ).methodOrNull?.apply {
            implementation!!.instructions
                .mapIndexedNotNull { i, insn ->
                    if (insn.opcode == Opcode.IGET_OBJECT &&
                        insn.toString().contains("onClose")) i else null
                }
                .reversed()
                .forEach { idx ->
                    val reg = getInstruction<OneRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
        }
    }
}
