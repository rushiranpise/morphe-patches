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
import app.template.patches.telegram.CanForwardMessageFingerprint
import app.template.patches.telegram.ChatActivityHasSelectedNoforwardsMessageFingerprint
import app.template.patches.telegram.ChatActivityIsPeerNoForwardsFingerprint
import app.template.patches.telegram.MessagesControllerIsChatNoForwardsChatFingerprint
import app.template.patches.telegram.MessagesControllerIsChatNoForwardsLongFingerprint
import app.template.patches.telegram.MessagesControllerIsPeerNoForwardsFingerprint
import app.template.patches.telegram.ProfileActivityIsPeerNoForwardsFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val telegramBypassContentRestrictionsPatch = bytecodePatch(
    name = "Bypass content restrictions",
    description = "Allows saving and forwarding content from restricted channels and chats.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // isChatNoForwards — both overloads
        MessagesControllerIsChatNoForwardsLongFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        MessagesControllerIsChatNoForwardsChatFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // isPeerNoForwards — all three classes
        MessagesControllerIsPeerNoForwardsFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        ChatActivityIsPeerNoForwardsFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        ProfileActivityIsPeerNoForwardsFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // MessageObject.canForwardMessage → always true
        CanForwardMessageFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // ChatActivity.hasSelectedNoforwardsMessage → always false (forward button always enabled)
        ChatActivityHasSelectedNoforwardsMessageFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Replace ALL reads of TLRPC$Message.noforwards field with const/4 0
        val msgNoforwardsFilter = fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/tgnet/TLRPC\$Message;",
            name = "noforwards",
            type = "Z",
        )
        Fingerprint(filters = listOf(msgNoforwardsFilter)).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                val indices = match.instructionMatches.map { it.index }
                for (index in indices.reversed()) {
                    val reg = getInstruction<TwoRegisterInstruction>(index).registerA
                    replaceInstruction(index, "const/4 v$reg, 0x0")
                }
            }
        }

        // Replace ALL reads of TLRPC$Chat.noforwards field with const/4 0
        val chatNoforwardsFilter = fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/tgnet/TLRPC\$Chat;",
            name = "noforwards",
            type = "Z",
        )
        Fingerprint(filters = listOf(chatNoforwardsFilter)).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                val indices = match.instructionMatches.map { it.index }
                for (index in indices.reversed()) {
                    val reg = getInstruction<TwoRegisterInstruction>(index).registerA
                    replaceInstruction(index, "const/4 v$reg, 0x0")
                }
            }
        }

        // Patch noforwards on request TL types (send/forward)
        listOf(
            "Lorg/telegram/tgnet/TLRPC\$TL_messages_forwardMessages;",
            "Lorg/telegram/tgnet/TLRPC\$TL_messages_sendMessage;",
            "Lorg/telegram/tgnet/TLRPC\$TL_messages_sendMedia;",
            "Lorg/telegram/tgnet/TLRPC\$TL_messages_sendMultiMedia;",
        ).forEach { definingClass ->
            val filter = fieldAccess(
                opcode = Opcode.IGET_BOOLEAN,
                definingClass = definingClass,
                name = "noforwards",
                type = "Z",
            )
            Fingerprint(filters = listOf(filter)).matchAllOrNull()?.forEach { match ->
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
}
