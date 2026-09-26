package app.template.patches.telegram.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency

private val isSecretMediaInstanceFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "isSecretMedia",
    returnType = "Z",
    parameters = emptyList(),
)

private val isSecretMediaStaticFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "isSecretMedia",
    returnType = "Z",
    parameters = listOf("Lorg/telegram/tgnet/TLRPC\$Message;"),
)

private val isSecretPhotoOrVideoFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "isSecretPhotoOrVideo",
    returnType = "Z",
    parameters = listOf("Lorg/telegram/tgnet/TLRPC\$Message;"),
)

private val shouldEncryptPhotoOrVideoFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "shouldEncryptPhotoOrVideo",
    returnType = "Z",
    parameters = listOf("I", "Lorg/telegram/tgnet/TLRPC\$Message;"),
)

private val isVoiceOnceFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "isVoiceOnce",
    returnType = "Z",
    parameters = emptyList(),
)

private val isRoundOnceFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "isRoundOnce",
    returnType = "Z",
    parameters = emptyList(),
)

private val needDrawBluredPreviewFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessageObject;",
    name = "needDrawBluredPreview",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * Telegram/Web 12.10.4 and Telegram Plus 12.10.3.0 expose SecretMediaViewer's
 * close routine through obfuscated methods. Earlier versions of this patch
 * attempted to identify the close routine by matching every Runnable field read
 * in SecretMediaViewer.e(ZZ)Z / its Plus equivalent.
 *
 * DEX verification of the supplied 12.10.x builds shows that this is unsafe:
 * the matched Runnable reads are lifecycle callbacks, including callbacks that
 * are immediately invoked with Runnable.run(). Replacing those reads with
 * const/4 0 can cause a null Runnable invocation and break the viewer.
 *
 * Therefore this patch deliberately does NOT modify SecretMediaViewer. The
 * MessageObject-level gates below are the only verified hooks retained.
 */
@Suppress("unused")
val telegramAntiDisappearingMediaPatch = bytecodePatch(
    name = "Anti-disappearing media",
    description = "Keeps view-once photos, videos and voice messages viewable indefinitely without modifying SecretMediaViewer lifecycle callbacks.",
) {
    compatibleWith(
        TELEGRAM_COMPATIBILITY,
        TELEGRAM_PLUS_COMPATIBILITY,
        TELEGRAM_WEB_COMPATIBILITY,
    )

    dependsOn(telegramSpoofDependency())

    execute {
        listOf(
            isSecretMediaInstanceFingerprint,
            isSecretMediaStaticFingerprint,
            isSecretPhotoOrVideoFingerprint,
            shouldEncryptPhotoOrVideoFingerprint,
            isVoiceOnceFingerprint,
            isRoundOnceFingerprint,
            needDrawBluredPreviewFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
                """,
            )
        }
    }
}
