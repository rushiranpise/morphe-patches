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
import app.template.patches.telegram.CanForwardMessageFingerprint
import app.template.patches.telegram.ChatActivityHasSelectedNoforwardsMessageFingerprint
import app.template.patches.telegram.ChatActivityIsPeerNoForwardsFingerprint
import app.template.patches.telegram.MessagesControllerIsChatNoForwardsChatFingerprint
import app.template.patches.telegram.MessagesControllerIsChatNoForwardsLongFingerprint
import app.template.patches.telegram.MessagesControllerIsPeerNoForwardsFingerprint
import app.template.patches.telegram.MessagesControllerIsUserNoForwardsLongFingerprint
import app.template.patches.telegram.MessagesControllerIsUserNoForwardsUserFullFingerprint
import app.template.patches.telegram.ProfileActivityIsPeerNoForwardsFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val telegramBypassContentRestrictionsPatch = bytecodePatch(
    name = "Bypass content restrictions",
    description = "Allows saving and forwarding content from restricted channels, chats, and users.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // isChatNoForwards — both overloads
        listOf(
            MessagesControllerIsChatNoForwardsLongFingerprint,
            MessagesControllerIsChatNoForwardsChatFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // isUserNoForwards — both overloads (DM forward restrictions)
        listOf(
            MessagesControllerIsUserNoForwardsLongFingerprint,
            MessagesControllerIsUserNoForwardsUserFullFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // isPeerNoForwards — all three call sites
        listOf(
            MessagesControllerIsPeerNoForwardsFingerprint,
            ChatActivityIsPeerNoForwardsFingerprint,
            ProfileActivityIsPeerNoForwardsFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // canForwardMessage → always true
        CanForwardMessageFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // hasSelectedNoforwardsMessage → false (forward button always enabled)
        ChatActivityHasSelectedNoforwardsMessageFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Patch all TLRPC$Message.noforwards field reads → false
        Fingerprint(filters = listOf(fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/tgnet/TLRPC\$Message;",
            name = "noforwards",
        ))).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                match.instructionMatches.map { it.index }.reversed().forEach { idx ->
                    val reg = getInstruction<TwoRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
            }
        }

        // Patch all TLRPC$Chat.noforwards field reads → false
        Fingerprint(filters = listOf(fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/tgnet/TLRPC\$Chat;",
            name = "noforwards",
        ))).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                match.instructionMatches.map { it.index }.reversed().forEach { idx ->
                    val reg = getInstruction<TwoRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
            }
        }
    }
}
