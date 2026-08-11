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
import app.template.patches.telegram.GetChannelDiffErrorFingerprint
import app.template.patches.telegram.GetRestrictionReasonFingerprint
import app.template.patches.telegram.LoadFullChatErrorFingerprint
import app.template.patches.telegram.MessageObjectIsHiddenSensitiveFingerprint
import app.template.patches.telegram.MessageObjectIsSensitiveFingerprint
import app.template.patches.telegram.MessagesControllerIsSensitiveFingerprint
import app.template.patches.telegram.SetContentSettingsFingerprint
import app.template.patches.telegram.ShowCantOpenAlertFingerprint
import app.template.patches.telegram.ShowSensitiveContentFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val telegramBypassChannelRestrictionsPatch = bytecodePatch(
    name = "Bypass channel restrictions",
    description = "Allows opening copyrighted, sensitive, and temporarily disabled channels.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // Force sensitive_enabled=true bypassing sensitive_can_change server flag
        SetContentSettingsFingerprint.method.addInstructions(0, "const/4 p1, 0x1")

        // No restriction reason = channel accessible
        GetRestrictionReasonFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // Suppress "can't open" alert dialog
        ShowCantOpenAlertFingerprint.method.addInstructions(0, "return-void")

        // Skip CHANNEL_PRIVATE / USER_BANNED error handling
        CheckChannelErrorFingerprint.method.addInstructions(0, "return-void")

        // Never flag content as sensitive
        MessagesControllerIsSensitiveFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Skip sensitive content dialog — execute the success runnable directly
        CheckSensitiveFingerprint.method.addInstructions(0, """
            if-eqz p4, :skip
            invoke-interface {p4}, Ljava/lang/Runnable;->run()V
            :skip
            return-void
        """)

        // Always report sensitive content as shown/enabled
        ShowSensitiveContentFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Per-message: never mark as sensitive
        MessageObjectIsSensitiveFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Per-message: never hide sensitive thumbnails
        MessageObjectIsHiddenSensitiveFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Suppress "no access" alert (4-param in 12.9.2)
        CreateNoAccessAlertFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // Error handlers → return void
        LoadFullChatErrorFingerprint.method.addInstructions(0, "return-void")
        GetChannelDiffErrorFingerprint.methodOrNull?.addInstructions(0, "return-void")

        // Allow opening any chat regardless of restriction state
        CheckCanOpenChat2Fingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        CheckCanOpenChat3Fingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        CheckCanOpenChat4Fingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Neutralize isRestrictedMessage field reads — replace with false constant
        val isRestrictedFilter = fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/messenger/MessageObject;",
            name = "isRestrictedMessage",
            type = "Z",
        )
        Fingerprint(filters = listOf(isRestrictedFilter)).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                val indices = match.instructionMatches.map { it.index }
                for (index in indices.reversed()) {
                    val reg = getInstruction<TwoRegisterInstruction>(index).registerA
                    replaceInstruction(index, "const/4 v$reg, 0x0")
                }
            }
        }
    }
}
