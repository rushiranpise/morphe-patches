package app.template.patches.telegram.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.CheckCanOpenChat2Fingerprint
import app.template.patches.telegram.CheckCanOpenChat3Fingerprint
import app.template.patches.telegram.CheckCanOpenChat4Fingerprint
import app.template.patches.telegram.CheckChannelErrorFingerprint
import app.template.patches.telegram.CheckSensitiveFingerprint
import app.template.patches.telegram.CreateNoAccessAlertFingerprint
import app.template.patches.telegram.DialogCellBuildLayoutFingerprint
import app.template.patches.telegram.DialogCellUpdateMessageThumbsFingerprint
import app.template.patches.telegram.GetChannelDiffErrorFingerprint
import app.template.patches.telegram.GetRestrictionReasonFingerprint
import app.template.patches.telegram.LoadFullChatErrorFingerprint
import app.template.patches.telegram.MessageObjectIsHiddenSensitiveFingerprint
import app.template.patches.telegram.MessageObjectIsSensitiveFingerprint
import app.template.patches.telegram.MessageObjectUpdateMessageTextFingerprint
import app.template.patches.telegram.MessagesControllerIsSensitiveFingerprint
import app.template.patches.telegram.SetContentSettingsFingerprint
import app.template.patches.telegram.ShowCantOpenAlertFingerprint
import app.template.patches.telegram.ShowSensitiveContentFingerprint
import app.template.patches.telegram.signature.telegramSpoofDependency
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val telegramBypassChannelRestrictionsPatch = bytecodePatch(
    name = "Bypass channel restrictions",
    description = "Allows opening, viewing, saving and forwarding content from restricted, " +
        "sensitive, and copyright-restricted channels.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {

        // ── Layer 1: null the source ──────────────────────────────────────────
        // getRestrictionReason(ArrayList) → null
        // Covers all call sites inside MessageObject and PhotoViewer.
        GetRestrictionReasonFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // ── Layer 2: null result registers in DialogCell call sites ───────────
        // DialogCell.buildLayout and updateMessageThumbs call getRestrictionReason
        // directly and pass the result to getMessageStringFormatted.
        // Replace the move-result-object AFTER each getRestrictionReason invoke
        // with const/4 0 on the same register — forcing null into the result slot
        // so TextUtils.isEmpty(null)=true and the restriction display block is skipped.
        // This fixes the dialogs list preview even for DB-cached messages.
        listOf(
            DialogCellBuildLayoutFingerprint,
            DialogCellUpdateMessageThumbsFingerprint,
        ).forEach { fp ->
            fp.method.apply {
                // Find all getRestrictionReason invoke sites in this method
                val matchIndices = fp.instructionMatches.map { it.index }
                // Work in reverse so earlier indices stay valid
                for (invokeIdx in matchIndices.reversed()) {
                    // The move-result-object immediately follows the invoke
                    val moveResultIdx = invokeIdx + 1
                    val reg = getInstruction<OneRegisterInstruction>(moveResultIdx).registerA
                    replaceInstruction(moveResultIdx, "const/4 v$reg, 0x0")
                }
            }
        }

        // ── Layer 3: block updateMessageText entirely ─────────────────────────
        // Prevents messageText and isRestrictedMessage from ever being overwritten.
        MessageObjectUpdateMessageTextFingerprint.method.addInstructions(0, "return-void")

        // ── Layer 4: replace all isRestrictedMessage READS → false ───────────
        Fingerprint(filters = listOf(fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/messenger/MessageObject;",
            name = "isRestrictedMessage",
            type = "Z",
        ))).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                match.instructionMatches.map { it.index }.reversed().forEach { idx ->
                    val reg = getInstruction<TwoRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
            }
        }

        // ── Layer 5: replace all isRestrictedMessage WRITES → false ──────────
        Fingerprint(filters = listOf(fieldAccess(
            opcode = Opcode.IPUT_BOOLEAN,
            definingClass = "Lorg/telegram/messenger/MessageObject;",
            name = "isRestrictedMessage",
            type = "Z",
        ))).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                match.instructionMatches.map { it.index }.reversed().forEach { idx ->
                    val reg = getInstruction<TwoRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
            }
        }

        // ── Sensitive content ─────────────────────────────────────────────────
        SetContentSettingsFingerprint.method.addInstructions(0, "const/4 p1, 0x1")
        ShowSensitiveContentFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        MessagesControllerIsSensitiveFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        CheckSensitiveFingerprint.method.addInstructions(0, """
            if-eqz p4, :skip
            invoke-interface {p4}, Ljava/lang/Runnable;->run()V
            :skip
            return-void
        """)
        MessageObjectIsSensitiveFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        MessageObjectIsHiddenSensitiveFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // ── Channel access errors ─────────────────────────────────────────────
        ShowCantOpenAlertFingerprint.method.addInstructions(0, "return-void")
        CheckChannelErrorFingerprint.method.addInstructions(0, "return-void")
        CreateNoAccessAlertFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)
        LoadFullChatErrorFingerprint.method.addInstructions(0, "return-void")
        GetChannelDiffErrorFingerprint.methodOrNull?.addInstructions(0, "return-void")

        // ── Chat open permission ──────────────────────────────────────────────
        listOf(
            CheckCanOpenChat2Fingerprint,
            CheckCanOpenChat3Fingerprint,
            CheckCanOpenChat4Fingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }
    }
}
