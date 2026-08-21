package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.DeleteMessagesByPushFingerprint
import app.template.patches.telegram.MarkMessagesAsDeletedFingerprint1
import app.template.patches.telegram.MarkMessagesAsDeletedFingerprint2
import app.template.patches.telegram.NotificationsControllerRemoveDeletedMessagesFingerprint

@Suppress("unused")
val telegramAntiDeletePatch = bytecodePatch(
    name = "Anti-delete messages",
    description = "Prevents messages deleted by other users from being removed locally.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // markMessagesAsDeleted(JIZZ) — p4=Z is the async/local-only flag.
        // true = user-initiated local delete (allow); false = server-push delete (block).
        MarkMessagesAsDeletedFingerprint1.method.addInstructions(0, """
            if-nez p4, :allow
            const/4 v0, 0x0
            return-object v0
            :allow
            nop
        """)

        // markMessagesAsDeleted(JArrayListZZII) — p4=Z same semantics.
        MarkMessagesAsDeletedFingerprint2.method.addInstructions(0, """
            if-nez p4, :allow
            const/4 v0, 0x0
            return-object v0
            :allow
            nop
        """)

        // Block server-push deletion path entirely
        DeleteMessagesByPushFingerprint.method.addInstructions(0, "return-void")

        // Suppress notification removal when messages are deleted server-side
        // Sig changed in 12.9.2: (LongSparseArray, Z)V
        NotificationsControllerRemoveDeletedMessagesFingerprint.method.addInstructions(0, "return-void")
    }
}
